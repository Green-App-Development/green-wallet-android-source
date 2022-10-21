package com.android.greenapp.presentation.main.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.android.greenapp.R
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.databinding.FragmentHomeBinding
import com.android.greenapp.domain.entity.WalletWithTokens
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.VLog
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 12.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class HomeFragment : DaggerFragment(), ViewPagerWalletsAdapter.ViewPagerWalletClicker {

	private val binding by viewBinding(FragmentHomeBinding::bind)

	@Inject
	lateinit var viewModelFactory: ViewModelFactory

	@Inject
	lateinit var effect: AnimationManager
	private val homeFragmentViewModel: HomeFragmentViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var walletDao: WalletDao

	@Inject
	lateinit var dialogMan: DialogManager

	private var job: Job? = null
	private var cryptoJob: Job? = null
	private var networkItemsJob: Job? = null

	private var handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception in init viewPagerWalletAdapter : $ex")
	}

	companion object {
		const val SHOW_DIALOG_DOUBLE = "show_dialog_double"
		const val SHOW_DIALOG_ONE = "show_dialog_one"
	}

	private var showDialogsDouble = false
	private var showDialogOne = false
	private var curCourseChia = 0.0
	private var curBalance = 0.0
	private var curNetwork: String = "Chia"
	private var curFingerPrint: Long? = null
	private var toHideBalance = false
	private var prevChosenPositionInViewPager = 0
	private var hasAtLeastOneWallet = true

	private lateinit var mainViewPagerWalletAdapter: ViewPagerWalletsAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		VLog.d("OnCreate HomeFragment : ")
		arguments?.let {
			showDialogsDouble = it.getBoolean(SHOW_DIALOG_DOUBLE, false)
			showDialogOne = it.getBoolean(SHOW_DIALOG_ONE, false)
		}
	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.fragment_home, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("OnViewCreated on HomeFragment")
		prevModeChanged()
		registerButtonClicks()
		initWalletTokenAdapter()
		updateViewDetails()
		initCurCryptoCourseUpdate()
		homeFragmentViewModel.requestOtherNotifItems()
		homeFragmentViewModel.changeCryptCourseEvery10Seconds()
	}


	private fun initCurCryptoCourseUpdate() {
		cryptoJob?.cancel()
		cryptoJob = lifecycleScope.launchWhenStarted {
			homeFragmentViewModel.curCryptoCourse.collect {
				it?.let {
					courseTradeUp(it.increased, it.percent)
					curCourseChia = it.price
					txtPrice.setText("${getShortNetworkType(it.network)} price: ${it.price} $")
					updateBalanceToDollarStr()
				}
			}
		}
	}


	private fun updateViewDetails() {
		lifecycleScope.launch {
			homeFragmentViewModel.flowBalanceIsHidden().asLiveData().observe(viewLifecycleOwner) {
				toHideBalance = it
				updateBalanceToDollarStr()
				initWalletTokenAdapter()
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	private fun initWalletTokenAdapter() {
		kotlin.runCatching {
			job?.cancel()
			job = lifecycleScope.launchWhenStarted {
				homeFragmentViewModel.getHomeAddedWalletWithTokensFlow().collect {
					for (wallet in it) {
						VLog.d("Wallet from DB  ->  : ${wallet}")
						for (tokenWallet in wallet.tokenWalletList) {
							VLog.d("Token list : $tokenWallet")
						}
					}
					initWalletListViewPager(it)
				}
			}
		}.onFailure {
			VLog.d("Exception  in getting walletWithTokens : ${it.message}")
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	@SuppressLint("SetTextI18n")
	private fun initWalletListViewPager(homeAddedList: List<WalletWithTokens>) {
		if (homeAddedList.isNotEmpty()) {

			mainViewPagerWalletAdapter =
				ViewPagerWalletsAdapter(
					curActivity(),
					this,
					effect = effect,
					homeAddedList,
					balanceIsHidden = toHideBalance
				)
			mainWalletViewPager.adapter = mainViewPagerWalletAdapter
			pageIndicator.count = homeAddedList.size
			pageIndicator.visibility = View.VISIBLE
			mainWalletViewPager.visibility = View.VISIBLE
			rel_no_wallet.visibility = View.GONE
			if (prevChosenPositionInViewPager >= homeAddedList.size)
				prevChosenPositionInViewPager = 0
			curBalance = homeAddedList[prevChosenPositionInViewPager].totalAmountInUSD
			curNetwork = homeAddedList[prevChosenPositionInViewPager].networkType
			pageIndicator.setSelected(prevChosenPositionInViewPager)
			mainWalletViewPager.setCurrentItem(prevChosenPositionInViewPager, true)
//            homeFragmentViewModel.updateCryptoCurrencyCourse(curNetwork)
			curFingerPrint = homeAddedList[prevChosenPositionInViewPager].fingerPrint
			updateBalanceToDollarStr()
			homeFragmentViewModel.saveHomeIsAddedWalletCounter(homeAddedList.size)
			hasAtLeastOneWallet = true
		} else {
			binding.apply {
				rel_no_wallet.visibility = View.VISIBLE
				mainWalletViewPager.visibility = View.GONE
				txtMyBalance.text = "0 USD"
				pageIndicator.visibility = View.INVISIBLE
			}
			hasAtLeastOneWallet = false
		}
	}

	private fun registerButtonClicks() {

		img_ic_up.setOnClickListener {
			if (hasAtLeastOneWallet)
				curActivity().move2SendFragment(curNetwork, curFingerPrint, shouldQRCleared = true)
			else
				curActivity().showBtmDialogCreateOrImportNewWallet(false)
		}

		img_qr.setOnClickListener {
			if (hasAtLeastOneWallet)
				curActivity().move2ReceiveFragment(curNetwork, curFingerPrint)
			else
				curActivity().showBtmDialogCreateOrImportNewWallet(false)
		}

		img_three_dots.setOnClickListener {
//            it.startAnimation(effect.getAnimation())
			showBottomSheetDialogSettings()

		}

		mainWalletViewPager.addOnPageChangeListener(object :
			ViewPager.OnPageChangeListener {
			override fun onPageScrolled(
				position: Int,
				positionOffset: Float,
				positionOffsetPixels: Int
			) {

			}

			@SuppressLint("SetTextI18n")
			override fun onPageSelected(position: Int) {
				VLog.d("Selected Position  MainViewPager -> : $position")
				pageIndicator.setSelected(position)
				prevChosenPositionInViewPager = position
				if (this@HomeFragment::mainViewPagerWalletAdapter.isInitialized) {
					curBalance = mainViewPagerWalletAdapter.walletList[position].totalAmountInUSD
					curNetwork = mainViewPagerWalletAdapter.walletList[position].networkType
//                    homeFragmentViewModel.updateCryptoCurrencyCourse(curNetwork)
					curFingerPrint =
						mainViewPagerWalletAdapter.walletList[position].fingerPrint
					updateBalanceToDollarStr()
				}
			}

			override fun onPageScrollStateChanged(state: Int) {

			}

		})

		rel_add_wallet.setOnClickListener {
			addWallet()
		}

	}

	private fun updateBalanceToDollarStr() {
		val chiaInUSD = curBalance
		val formattedBalance = String.format("%.2f", chiaInUSD).replace(",", ".")
		val balance = "⁓${formattedBalance} USD"
		txtMyBalance.apply {
			if (!this@HomeFragment::mainViewPagerWalletAdapter.isInitialized) {
				text = "0 USD"
			} else if (toHideBalance) {
				text = "***** USD"
			} else {
				text = balance
			}
		}
	}


	private fun courseTradeUp(tradeUp: Boolean, cost_trade: String) {
		txtTradePercent.text = "$cost_trade %"
		if (tradeUp) {
			binding.relCourseTrade.background.setTint(
				ContextCompat.getColor(
					curActivity(),
					R.color.green
				)
			)
			binding.imgPolygon.setImageResource(R.drawable.ic_polygon_up)
		} else {
			binding.relCourseTrade.background.setTint(
				ContextCompat.getColor(
					curActivity(),
					R.color.red_mnemonic
				)
			)
			binding.imgPolygon.setImageResource(R.drawable.ic_polygon_down)
		}
	}

	private fun showBottomSheetDialogSettings() {
		val dialog = BottomSheetDialog(curActivity(), R.style.AppBottomSheetDialogTheme)
		val sheetView = curActivity().layoutInflater.inflate(R.layout.dialog_bottom_settings, null)
		dialog.setContentView(sheetView)
		registerButtonClicksInDialogSettings(dialog)
		dialog.show()
	}


	@SuppressLint("ClickableViewAccessibility")
	private fun registerButtonClicksInDialogSettings(dialog: Dialog) {
		intBtmSettingDialog(dialog)
		curActivity().bottomSettingDialog = dialog
		dialog.findViewById<RadioGroup>(R.id.btnRadioGroupMode)
			.setOnCheckedChangeListener { p0, p1 ->
				val nightMode = dialog.findViewById<RadioButton>(R.id.btnNightMode).isChecked
				VLog.d("OnChecked Mode Change Clicked : $nightMode")
//                dialog.dismiss()
				curActivity().mainViewModel.saveNightIsOn(nightMode)
			}

		dialog.findViewById<SwitchCompat>(R.id.btn_hide_balance_switch)
			.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
				override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
					curActivity().mainViewModel.saveBalanceIsHidden(p1)
				}
			})

		dialog.findViewById<SwitchCompat>(R.id.btn_push_notif_switch)
			.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
				override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
					curActivity().mainViewModel.savePushNotifIsOn(p1)
				}
			})

		dialog.findViewById<RelativeLayout>(R.id.relNotif).setOnClickListener {
			curActivity().move2NotificationFragment()
		}

		dialog.findViewById<RelativeLayout>(R.id.relSupportCall).setOnClickListener {
			curActivity().move2SupportFragmentFromBtmDialog()
		}

		dialog.findViewById<RelativeLayout>(R.id.relAllSettings).setOnClickListener {
			curActivity().move2AllSettingsFragment()
		}

		dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
			override fun onDismiss(p0: DialogInterface?) {
				VLog.d("Setting Dialog dismissed or closed")
				homeFragmentViewModel.savePrevModeChanged(changed = false)
				initWalletTokenAdapter()
			}
		})


	}


	private fun prevModeChanged() {
		lifecycleScope.launch {
			val prevModeChanged = homeFragmentViewModel.prevModeChanged()
			VLog.d("PrevModeChanged Value : $prevModeChanged")
			if (prevModeChanged) {
				homeFragmentViewModel.savePrevModeChanged(changed = false)
				showBottomSheetDialogSettings()
			}
		}
	}

	private fun intBtmSettingDialog(dialog: Dialog) {
		lifecycleScope.launch {
			val mainViewModel = curActivity().mainViewModel

			homeFragmentViewModel.flowBalanceIsHidden().asLiveData().observe(viewLifecycleOwner) {
				dialog.findViewById<SwitchCompat>(R.id.btn_hide_balance_switch).isChecked = it
			}

			dialog.findViewById<SwitchCompat>(R.id.btn_push_notif_switch).isChecked =
				mainViewModel.getPushNotifIsOn()

			if (mainViewModel.getNightModeIsOn()) {
				dialog.findViewById<RadioButton>(R.id.btnNightMode).isChecked = true
			} else {
				dialog.findViewById<RadioButton>(R.id.btnLightMode).isChecked = true
			}
		}
	}


	private fun curActivity() = requireActivity() as MainActivity


	override fun addWallet() {
		curActivity().showBtmDialogCreateOrImportNewWallet(hasAtLeastOneWallet)
	}


	override fun allWallet() {
		curActivity().move2AllWalletFragment()
	}

	override fun impToken(fingerPrint: Long, main_puzzle_hash: String) {
		curActivity().move2FragmentImportToken(fingerPrint, main_puzzle_hash)
	}

	override fun onStart() {
		super.onStart()
		VLog.d("On Start on HomeFragment")
	}

	override fun onResume() {
		super.onResume()
		VLog.d("On Resume on HomeFragment")
	}

	override fun onPause() {
		super.onPause()
		VLog.d("On Pause on HomeFragment")
	}

	override fun onStop() {
		super.onStop()
		VLog.d("On Stop on HomeFragment")
	}

	override fun onDestroyView() {
		super.onDestroyView()
		VLog.d("On DestroyView on HomeFragment")
		job?.cancel()
		cryptoJob?.cancel()
	}

	override fun onDestroy() {
		super.onDestroy()
		VLog.d("On Destroy HomeFragment")
	}


}
