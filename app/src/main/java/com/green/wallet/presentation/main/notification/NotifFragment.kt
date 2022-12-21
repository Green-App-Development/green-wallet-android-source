package com.green.wallet.presentation.main.notification

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentNotificationBinding
import com.green.wallet.domain.domainmodel.notification.NotificationItem
import com.green.wallet.presentation.custom.CustomSpinner
import com.green.wallet.presentation.custom.formattedDateForTransaction
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.custom.getShortNetworkType
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.transaction.TransactionSortingAdapter
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getDrawableResource
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.viewBinding
import com.example.common.tools.*
import dagger.android.support.DaggerDialogFragment
import kotlinx.android.synthetic.main.dialog_notification_detail.*
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by bekjan on 31.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class NotifFragment : DaggerDialogFragment(),
	NotifSectionAdapter.ParentNotifListener {


	companion object {
		const val SHOW_GREEN_APP_NOTIF = "show_green_notif_key"
	}


	private val binding: FragmentNotificationBinding by viewBinding(FragmentNotificationBinding::bind)
	private lateinit var dateAdapter: TransactionSortingAdapter
	private var prevCLickedStatus: TextView? = null
	private lateinit var notifSectionAdapter: NotifSectionAdapter


	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: NotifViewModel by viewModels { viewModelFactory }

	private var queryJob: Job? = null
	private var show_green_notif = false


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		show_green_notif = arguments?.getBoolean(SHOW_GREEN_APP_NOTIF, false) ?: false
	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_notification, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initStatusBarColor()
		initNotifSectionItems()
		initSortingAdapterByDate()
		registerClicks()
		initStatusHorizontalView()
		initSortingByStatus()
		retrievingNotifs()
		updateQuery()
		initSortingByAmount()
	}

	private fun initSortingByAmount() {
		edtSearchNotifs.addTextChangedListener {
			updateQuery()
		}
	}

	private fun updateQuery() {
		viewModel.updateQuery(
			getCurSearchAmount(),
			getCurChosenNotifStatus(),
			getCurSearchTransByDateCreatedExceptYesterday(),
			getCurSearchByForYesterdayStart(),
			getCurSearchByForYesterdayEnds()
		)
//		retrievingNotifs()
	}

	private fun getCurChosenNotifStatus(): Status? {
		if (prevCLickedStatus == null || prevCLickedStatus?.tag == "ALL")
			return null
		if (prevCLickedStatus!!.tag == "ENROLLED")
			return Status.Incoming
		else if (prevCLickedStatus!!.tag == "WRITE_OFF")
			return Status.Outgoing
		return Status.OTHER
	}

	private fun retrievingNotifs() {
		queryJob?.cancel()
		queryJob = lifecycleScope.launch {
			viewModel.notifSectionItems.collect {
				it?.let {
					notifSectionAdapter.updateNotifSectionList(it)
				}
			}
		}
	}

	private fun initNotifSectionItems() {
		notifSectionAdapter = NotifSectionAdapter(curActivity(), this)
		binding.recViewNotifSection.adapter = notifSectionAdapter
	}

	private fun initSortingByStatus() {
		val curChosenStatus = if (show_green_notif) binding.txtOther else binding.txtSortingAll
		prevCLickedStatus = curChosenStatus
		for (txt in binding.linearSortingByStatus.children) {
			if (txt is TextView) {
				sortingCategoryUnClicked(txt)
				txt.setOnClickListener {
					if (it == prevCLickedStatus)
						return@setOnClickListener
					sortingCategoryUnClicked(prevCLickedStatus)
					sortingCategoryClicked(txt)
					prevCLickedStatus = txt
					updateQuery()
				}
			}
		}
		if (show_green_notif) {
			binding.apply {
				horizontalSorting.post {
					horizontalSorting.scrollTo(txtOther.right, txtOther.top)
				}
			}
		}
		sortingCategoryClicked(curChosenStatus)
	}

	private fun getCurSearchTransByDateCreatedExceptYesterday(): Long? {
		if (!this::dateAdapter.isInitialized) return null
		VLog.d("Date Position Selected : ${dateAdapter.selectedPosition}")
		return when (dateAdapter.selectedPosition) {
			0 -> null
			1 -> calculateMillisecondsPassedSinceMidNight(System.currentTimeMillis())
			2 -> null
			3 -> calculateMillisecondsPassedSinceLastWeek(System.currentTimeMillis())
			4 -> calculateMillisecondsPassedSinceLastMonth(System.currentTimeMillis())
			else -> null
		}
	}


	private fun sortingCategoryUnClicked(txt: TextView?) {
		txt?.apply {
			background.setTint(curActivity().getColorResource(R.color.bcg_sorting_txt_category))
			setTextColor(curActivity().getColorResource(R.color.sorting_txt_category))
		}
	}


	private fun sortingCategoryClicked(txt: TextView?) {
		txt?.apply {
			background.setTint(curActivity().getColorResource(R.color.green))
			setTextColor(curActivity().getColorResource(R.color.white))
		}
	}

	private fun initStatusHorizontalView() {
		val display =
			(requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
		val threeDivided = display / 3 - 30
		for (txt in binding.linearSortingByStatus.children) {
			if (txt is TextView)
				txt.layoutParams.width = threeDivided
		}
	}


	private fun registerClicks() {
		binding.apply {

			imgFilter.setOnClickListener {
				dateSpinner.performClick()
				imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_enabled))
			}

			backLayout.setOnClickListener {
				curActivity().popBackStackOnce()
			}

		}
	}


	private fun initSortingAdapterByDate() {
		curActivity().apply {
			dateAdapter = TransactionSortingAdapter(
				curActivity(),
				listOf(
					getStringResource(R.string.notifications_all),
					getStringResource(R.string.notifications_today),
					getStringResource(R.string.notifications_yesterday),
					getStringResource(R.string.notifications_week),
					getStringResource(R.string.notifications_month),
					getStringResource(R.string.notifications_year)
				)
			)
		}
		binding.dateSpinner.adapter = dateAdapter

		binding.dateSpinner.setSpinnerEventsListener(object :
			CustomSpinner.OnSpinnerEventsListener {
			override fun onSpinnerOpened(spin: Spinner?) {
				binding.imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_enabled))
			}

			override fun onSpinnerClosed(spin: Spinner?) {
				binding.imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_not_enabled))
			}
		})

		binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				dateAdapter.selectedPosition = p2
				updateQuery()
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}


	}

	private fun getCurSearchByForYesterdayStart(): Long? {
		if (dateAdapter.selectedPosition == 2) {
			return calculateMillisecondsPassedSinceYesterday(System.currentTimeMillis())
		}
		return null
	}

	private fun getCurSearchByForYesterdayEnds(): Long? {
		if (dateAdapter.selectedPosition == 2) {
			return calculateMillisecondsPassedSinceMidNight(System.currentTimeMillis())
		}
		return null
	}


	private fun getCurSearchAmount(): Double? {
		if (edtSearchNotifs == null) return null
		if (edtSearchNotifs.text.isNullOrEmpty()) return null
		return edtSearchNotifs.text.toString().toDouble()
	}


	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.primary_app_background)
		}
	}


	private fun curActivity() = requireActivity() as MainActivity

	override fun onNotifClicked(notificationItem: NotificationItem) {

		val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
		dialog.setContentView(R.layout.dialog_notification_detail)
		val width = resources.displayMetrics.widthPixels
		dialog.apply {
			addingDoubleDotsTxt(findViewById(R.id.txtDate))
			addingDoubleDotsTxt(findViewById(R.id.txtCountCoins))
			addingDoubleDotsTxt(findViewById(R.id.txtCommission))
			addingDoubleDotsTxt(findViewById(R.id.txtHeightBlocks))
			findViewById<LinearLayout>(R.id.back_layout).setOnClickListener {
				dialog.dismiss()
			}
		}
		initNotifDetails(notificationItem, dialog)
		dialog.window?.setLayout(
			width,
			WindowManager.LayoutParams.WRAP_CONTENT
		)
		dialog.show()
	}

	@SuppressLint("SetTextI18n")
	private fun initNotifDetails(notifItem: NotificationItem, dialog: Dialog) {
		dialog.apply {
			edtDate.setText(formattedDateForTransaction(curActivity(), notifItem.created_at_time))
			edtCountCoins.setText(
				"${formattedDoubleAmountWithPrecision(notifItem.amount)} ${
					notifItem.code
				}"
			)
			edtCommission.setText(
				"${formattedDoubleAmountWithPrecision(notifItem.commission)} ${
					getShortNetworkType(
						notifItem.networkType
					)
				}"
			)
			edtHeightBlocks.setText("${notifItem.height}")
		}
	}

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

}
