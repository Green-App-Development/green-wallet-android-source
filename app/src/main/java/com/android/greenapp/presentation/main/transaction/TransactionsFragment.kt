package com.android.greenapp.presentation.main.transaction

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentTransactionsBinding
import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.Status
import com.android.greenapp.presentation.tools.getColorResource
import com.android.greenapp.presentation.tools.getDrawableResource
import com.android.greenapp.presentation.tools.getStringResource
import com.android.greenapp.presentation.tools.pxToDp
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Created by bekjan on 11.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class TransactionsFragment : DaggerFragment(), TransactionItemAdapter.TransactionListener {


	companion object {
		const val FINGER_PRINT_KEY = "finger_print_key"
		const val ADDRESS_KEY = "address_key"
	}

	@Inject
	lateinit var animManager: AnimationManager

	private lateinit var dateAdapter: TransactionSortingAdapter

	private lateinit var networkAdapter: TransactionSortingAdapter

	private val binding by viewBinding(FragmentTransactionsBinding::bind)

	private lateinit var transactionItemAdapter: TransactionItemAdapter

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: TransactionsViewModel by viewModels { viewModelFactory }

	private var prevClickedStatus: TextView? = null

	private var updateTransJob: Job? = null

	//XCC  chives
	//XCH  chia
	private var currentFingerPrint: Long? = null
	private var currentAddress: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			val incoming = it.getLong(FINGER_PRINT_KEY)
			currentFingerPrint = if (incoming == 0L) null else incoming
			curActivity().shouldGoBackHomeFragmentFromTransactions = currentFingerPrint == null
			val address = it.getString(ADDRESS_KEY,"")
			currentAddress = if (address == "") null else address
		}
		VLog.d("Got Incoming arguments For Transactions Fragment : $currentFingerPrint ")
	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_transactions, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerClicks()
		initDateAdapter()
		initNetworkTypeAdapter()
		initStatusHorizontalView()
		initSortingByStatusClicks()
		initTransactionItemAdapter()
		sortingByHeightAndSum()
		updateTransactions()
		initSwipeRefreshLayout()
	}



	private fun initSwipeRefreshLayout() {
		binding.swipeRefresh.apply {
			setOnRefreshListener {
				viewModel.swipedRefreshClicked {
					if (this@TransactionsFragment.isVisible) {
						isRefreshing = false
					}
				}
			}
			setColorSchemeResources(R.color.green)

		}
	}

	private fun updateTransactions() {
		updateTransJob?.cancel()
		updateTransJob = lifecycleScope.launch {
			viewModel.getAllQueriedFlowTransactionList(
				currentAddress,
				getCurSearchAmount(),
				getCurChosenNetworkType(),
				getCurChosenStatus(),
				getCurSearchTransByDateCreatedExceptYesterday(),
				getCurSearchByForYesterdayStart(),
				getCurSearchByForYesterdayEnds()
			).collectLatest { transList ->
				binding.apply {
					if (transList.isNotEmpty()) {
						recTransactionItems.visibility = View.VISIBLE
						txtNoTrans.visibility = View.GONE
						updateTransactionItems(transList)
					} else {
						recTransactionItems.visibility = View.GONE
						txtNoTrans.visibility = View.VISIBLE
					}
				}
			}
		}
	}

	private fun initStatusHorizontalView() {
		val display =
			(requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
		val threeDivided = display / 3 - 15
		for (txt in binding.linearSortingByStatus.children) {
			if (txt is TextView)
				txt.layoutParams.width = threeDivided
		}
	}


	private fun sortingByHeightAndSum() {
		binding.edtSearchTrans.addTextChangedListener {
			updateTransactions()
		}
	}

	private suspend fun updateTransactionItems(transList: List<Transaction>) {
		delay(50)
		val recHeight = binding.recTransactionItems.height
		val dp = curActivity().pxToDp(recHeight)
		val atLeastItemCount = (dp / 45) - 2
		VLog.d("At Least Item Count of RecyclerView Items : $atLeastItemCount")
		for (tran in transList) {
			VLog.d("Update Trans Adapter : $tran")
		}
		transactionItemAdapter.itemCountFitsScreen = atLeastItemCount
		transactionItemAdapter.updateTransactionList(transList)
		transactionItemAdapter.notifyDataSetChanged()
	}


	private fun initSortingByStatusClicks() {
		for (txt in binding.linearSortingByStatus.children) {
			if (txt is TextView) {
				sortingCategoryUnClicked(txt)
				txt.setOnClickListener {
					if (it == prevClickedStatus)
						return@setOnClickListener
					sortingCategoryUnClicked(prevClickedStatus)
					sortingCategoryClicked(txt)
					prevClickedStatus = txt
					updateTransactions()
				}
			}
		}
		sortingCategoryClicked(txtSortingAll)
		prevClickedStatus = txtSortingAll
	}


	private fun sortingCategoryClicked(txt: TextView?) {
		txt?.apply {
			background.setTint(curActivity().getColorResource(R.color.green))
			setTextColor(curActivity().getColorResource(R.color.white))
		}
	}

	private fun sortingCategoryUnClicked(txt: TextView?) {
		txt?.apply {
			background.setTint(curActivity().getColorResource(R.color.bcg_sorting_txt_category))
			setTextColor(curActivity().getColorResource(R.color.sorting_txt_category))
		}
	}

	private fun initTransactionItemAdapter() {
		transactionItemAdapter = TransactionItemAdapter(effect = animManager, curActivity(), this)
		binding.recTransactionItems.apply {
			adapter = transactionItemAdapter
			layoutManager = LinearLayoutManager(curActivity())
		}
	}

	private fun initNetworkTypeAdapter() {
		lifecycleScope.launch {

			val distinctNetworkTypes =
				viewModel.getDistinctNetworkTypeValues().toMutableList()

			distinctNetworkTypes.add(0, curActivity().getStringResource(R.string.transactions_all))

			networkAdapter =
				TransactionSortingAdapter(
					curActivity(),
					distinctNetworkTypes
				)
			binding.networkSpinnerTrans.adapter = networkAdapter

			binding.networkSpinnerTrans.onItemSelectedListener =
				object : AdapterView.OnItemSelectedListener {
					override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
						networkAdapter.selectedPosition = p2
						updateTransactions()
						updateCurChosenNetworkTypeTxt(distinctNetworkTypes[p2], p2)
					}

					override fun onNothingSelected(p0: AdapterView<*>?) {

					}

				}
		}
	}

	private fun updateCurChosenNetworkTypeTxt(network: String, position: Int) {
//		var len: Int
//		if (position == 0)
//			len = 145
//		else if (network.contains("Chia"))
//			len = 350
//		else
//			len = 400
//
//		rel_txt_network.layoutParams.width = len
		binding.txtNetworkType.text = network
	}

	private fun getCurChosenStatus(): Status? {
		if (prevClickedStatus == null || prevClickedStatus!!.tag == "txtSortingAll") return null
		return when (prevClickedStatus!!.tag.toString()) {
			Status.Incoming.toString() -> {
				Status.Incoming
			}
			Status.Outgoing.toString() -> {
				Status.Outgoing
			}
			Status.InProgress.toString() -> {
				Status.InProgress
			}
			else -> {
				null
			}
		}
	}

	private fun getCurSearchAmount(): Double? {
		if (edtSearchTrans.text.isNullOrEmpty()) return null
		return edtSearchTrans.text.toString().toDouble()
	}

	private fun getCurSearchTransByDateCreatedExceptYesterday(): Long? {
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

	private fun getCurChosenNetworkType(): String? {
		if (!this::networkAdapter.isInitialized) return null
		if (networkAdapter.selectedPosition == 0)
			return null

		return networkAdapter.options[networkAdapter.selectedPosition]
	}


	private fun initDateAdapter() {
		curActivity().apply {
			dateAdapter = TransactionSortingAdapter(
				curActivity(),
				listOf(
					getStringResource(R.string.transactions_all),
					getStringResource(R.string.transactions_today),
					getStringResource(R.string.transactions_yesterday),
					getStringResource(R.string.transactions_last_week),
					getStringResource(R.string.transactions_last_month)
				)
			)
		}
		binding.dateSpinner.adapter = dateAdapter

		binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				dateAdapter.selectedPosition = p2
				updateTransactions()
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}

		binding.dateSpinner.setSpinnerEventsListener(object :
			CustomSpinner.OnSpinnerEventsListener {
			override fun onSpinnerOpened(spin: Spinner?) {
				binding.imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_enabled))
			}

			override fun onSpinnerClosed(spin: Spinner?) {
				binding.imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_not_enabled))
			}
		})

	}

	private fun registerClicks() {

		binding.apply {

			imgFilter.setOnClickListener {
				it.startAnimation(animManager.getBtnEffectAnimation())
				binding.dateSpinner.performClick()
			}

			relTxtNetwork.setOnClickListener {
				binding.networkSpinnerTrans.performClick()
			}


		}

		animManager.animateArrowIconCustomSpinner(network_spinner_trans, imgArrow)

	}

	private fun showTransactionDetails(transaction: Transaction) {
		val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
		dialog.setContentView(R.layout.dialog_transaction_details)
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
		initTransDetails(dialog, transaction)
		dialog.window?.setLayout(
			width,
			WindowManager.LayoutParams.WRAP_CONTENT
		)
		dialog.show()
	}

	@SuppressLint("SetTextI18n")
	private fun initTransDetails(dialog: Dialog, transaction: Transaction) {
		VLog.d("Current Transaction Clicked -> : $transaction")
		dialog.apply {

			val formattedDate = formattedDateForTransaction(
				curActivity(),
				transaction.created_at_time
			)

			VLog.d("Formatted date for tran details : $formattedDate")

			findViewById<TextView>(R.id.edtDate).text = formattedDate

			findViewById<TextView>(R.id.edtAmountTrans).text =
				"${formattedDoubleAmountWithPrecision(transaction.amount)} ${
					getShortNetworkType(
						transaction.networkType
					)
				}"
			findViewById<TextView>(R.id.edtCommission).text =
				"${formattedDoubleAmountWithPrecision(transaction.fee_amount)} ${
					getShortNetworkType(
						transaction.networkType
					)
				}"
			findViewById<TextView>(R.id.edtHeightBlocks).text = "${transaction.confirmed_at_height}"
		}
	}


	override fun onDestroyView() {
		super.onDestroyView()
		VLog.d("TransactionFragment On DestroyView")
	}

	override fun onDestroy() {
		super.onDestroy()

	}

	private fun curActivity() = requireActivity() as MainActivity

	override fun onTransactionItemClicked(transaction: Transaction) {

	}


}
