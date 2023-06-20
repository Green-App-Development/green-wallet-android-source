package com.green.wallet.presentation.main.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.green.wallet.presentation.tools.VLog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.green.wallet.R
import com.green.wallet.data.local.WalletDao
import com.green.wallet.databinding.FragmentHomeBinding
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.tools.preventDoubleClick
import com.green.wallet.presentation.viewBinding
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.pageIndicator
import kotlinx.android.synthetic.main.fragment_manage_wallet_beta.*
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.*
import javax.inject.Inject


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
	lateinit var connectionLiveData: ConnectionLiveData

	@Inject
	lateinit var dialogMan: DialogManager

	@Inject
	lateinit var viewPagerState: ViewPagerPosition

	private var walletsJob: Job? = null
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

	private val requestPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it ->
			it.entries.forEach {
				if (it.value) {
					curActivity().move2ScannerFragment(curFingerPrint, curNetwork)
				} else
					Toast.makeText(
						curActivity(),
						curActivity().getStringResource(R.string.camera_permission_missing),
						Toast.LENGTH_SHORT
					)
						.show()
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
		VLog.d("OnViewCreated on HomeFragment $viewPagerState -> ${viewPagerState.pagerPosition}")
		prevModeChanged()
		binding.registerButtonClicks()
		initWalletTokenAdapter()
		updateViewDetails()
		initCurCryptoCourseUpdate()
//		homeFragmentViewModel.changeCryptCourseEvery10Seconds()
		initSwipeRefreshLayout()
		homeFragmentViewModel.updateCryptoCurrencyCourse("Chia Network")

	}

	private fun initSwipeRefreshLayout() {
		binding.swipeRefresh.apply {
			setOnRefreshListener {
				VLog.d("Is Online ${connectionLiveData.isOnline}")
				if (connectionLiveData.isOnline) {
					homeFragmentViewModel.swipedRefreshClicked {
						if (this@HomeFragment.isVisible) {
							isRefreshing = false
						}
					}
				} else {
					isRefreshing = false
					dialogMan.showNoInternetTimeOutExceptionDialog(curActivity()) {

					}
				}
			}
			setColorSchemeResources(R.color.green)
			isOneHomeFragment = true
		}
	}


	private fun initCurCryptoCourseUpdate() {
		cryptoJob?.cancel()
		cryptoJob = lifecycleScope.launchWhenStarted {
			homeFragmentViewModel.curCryptoCourse.collect {
				it?.let {
					courseTradeUp(it.increased, it.percent)
					curCourseChia = it.price
					txtPrice.setText(
						"${getShortNetworkType(it.network)} price: ${
							formattedDoubleAmountWithPrecision(
								it.price
							)
						}$"
					)
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
			walletsJob?.cancel()
			walletsJob = lifecycleScope.launch {
				repeatOnLifecycle(Lifecycle.State.STARTED) {
					homeFragmentViewModel.getHomeAddedWalletWithTokensFlow().collect {
//						for (wallet in it) {
//							VLog.d("Wallet from DB  ->  : $wallet")
//							for (tokenWallet in wallet.tokenWalletList) {
//								VLog.d("Token list : $tokenWallet")
//							}
//						}
						initWalletListViewPager(it)
					}
				}
			}
		}.onFailure {
			VLog.d("Exception  in getting walletWithTokens : ${it.message}")
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	@SuppressLint("SetTextI18n")
	private suspend fun initWalletListViewPager(homeAddedList: List<WalletWithTokens>) {
		if (homeAddedList.isNotEmpty()) {
			mainViewPagerWalletAdapter =
				ViewPagerWalletsAdapter(
					curActivity(),
					this,
					effect = effect,
					homeAddedList,
					balanceIsHidden = toHideBalance
				)
			initSettingIcon(homeAddedList[0].address)
			mainWalletViewPager.adapter = mainViewPagerWalletAdapter
			pageIndicator.count = homeAddedList.size
			pageIndicator.visibility = View.VISIBLE
			rel_no_wallet.visibility = View.GONE
			if (viewPagerState.pagerPosition >= homeAddedList.size) {
				viewPagerState.pagerPosition = 0
			}
			curBalance = homeAddedList[viewPagerState.pagerPosition].totalAmountInUSD
			curNetwork = homeAddedList[viewPagerState.pagerPosition].networkType
			homeFragmentViewModel.updateCryptoCurrencyCourse(homeAddedList[viewPagerState.pagerPosition].networkType)
			pageIndicator.setSelected(viewPagerState.pagerPosition)
//            homeFragmentViewModel.updateCryptoCurrencyCourse(curNetwork)
			curFingerPrint = homeAddedList[viewPagerState.pagerPosition].fingerPrint
			updateBalanceToDollarStr()
			homeFragmentViewModel.saveHomeIsAddedWalletCounter(homeAddedList.size)
			hasAtLeastOneWallet = true
			binding.apply {
				mainWalletViewPager.visibility = View.VISIBLE
				mainWalletViewPager.setCurrentItem(viewPagerState.pagerPosition, true)
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
						viewPagerState.pagerPosition = position
						if (this@HomeFragment::mainViewPagerWalletAdapter.isInitialized) {
							curBalance =
								mainViewPagerWalletAdapter.walletList[position].totalAmountInUSD
							curNetwork = mainViewPagerWalletAdapter.walletList[position].networkType
							curFingerPrint =
								mainViewPagerWalletAdapter.walletList[position].fingerPrint
							updateBalanceToDollarStr()
							initSettingIcon(mainViewPagerWalletAdapter.walletList[position].address)
							homeFragmentViewModel.updateCryptoCurrencyCourse(
								mainViewPagerWalletAdapter.walletList[position].networkType
							)
						}
					}

					override fun onPageScrollStateChanged(state: Int) {

					}

				})
			}
			delay(1000L)
			val rect = Rect()
			binding.apply {
				mainWalletViewPager.getGlobalVisibleRect(rect)
				swipeRefresh.topY = rect.top - 40
				swipeRefresh.bottomY = rect.bottom
			}

		} else {
			binding.apply {
				relNoWallet.visibility = View.VISIBLE
				mainWalletViewPager.visibility = View.GONE
				txtMyBalance.text = "0 USD"
				pageIndicator.visibility = View.INVISIBLE
				icSetting.visibility = View.GONE
			}
			hasAtLeastOneWallet = false
		}
	}

	private fun FragmentHomeBinding.registerButtonClicks() {

		icHomePlus.setOnClickListener {
			it.isEnabled = false
			constraintCommentPlus.visibility = View.VISIBLE
			Handler(Looper.getMainLooper()).postDelayed({
				constraintCommentPlus.visibility = View.GONE
				it.isEnabled = true
			}, 2000L)

		}

		relSend.setOnClickListener {
			if (hasAtLeastOneWallet)
				curActivity().move2SendFragment(curNetwork, curFingerPrint, shouldQRCleared = true)
			else
				curActivity().showBtmDialogCreateOrImportNewWallet(false)
		}

		relReceive.setOnClickListener {
			if (hasAtLeastOneWallet)
				curActivity().move2ReceiveFragment(curNetwork, curFingerPrint)
			else
				curActivity().showBtmDialogCreateOrImportNewWallet(false)
		}

		relHistory.setOnClickListener {
			curActivity().move2TransactionsFragment(0)
		}

		imgThreeDots.setOnClickListener {
			it.preventDoubleClick()
//            it.startAnimation(effect.getAnimation())
			showBottomSheetDialogSettings()
		}

		relAddWallet.setOnClickListener {
			addWallet()
		}

		icHomeQr.setOnClickListener {
			getMainActivity().move2BtmCreateOfferDialog()
			return@setOnClickListener
			if (hasAtLeastOneWallet)
				requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
			else
				curActivity().showBtmDialogCreateOrImportNewWallet(false)
		}

	}

	private fun initSettingIcon(address: String) {
		binding.icSetting.setOnClickListener {
			getMainActivity().move2WalletSettings(address = address)
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
			it.preventDoubleClick()
			curActivity().move2NotificationFragment()
		}

		dialog.findViewById<RelativeLayout>(R.id.relSupportCall).setOnClickListener {
			it.preventDoubleClick()
			curActivity().move2SupportFragmentFromBtmDialog()
		}

		dialog.findViewById<RelativeLayout>(R.id.relAllSettings).setOnClickListener {
			it.preventDoubleClick()
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

	override fun impToken(fingerPrint: Long, main_puzzle_hashes: List<String>, address: String) {
		curActivity().move2FragmentImportToken(
			fingerPrint,
			main_puzzle_hashes.toString(),
			networkType = "Chia Network",
			address = address
		)
	}

	private var jobUnderDev: Job? = null

	fun onCommentUnderDevClicked() {
		if (jobUnderDev?.isActive == true) return
		jobUnderDev = lifecycleScope.launch {
			val totalWidthInDp =
				curActivity().convertPixelToDp(binding.swipeRefresh.width.toFloat())
			val lenItem = totalWidthInDp / 3
			val params = binding.dotConstraintComment.layoutParams as ViewGroup.MarginLayoutParams
			params.setMargins(0, 0, lenItem, 0)
			binding.dotConstraintComment.layoutParams = params
			binding.constraintBtmNavViewComment.visibility = View.VISIBLE
			delay(2000)
			binding.constraintBtmNavViewComment.visibility = View.GONE
		}
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
		walletsJob?.cancel()
		jobUnderDev?.cancel()
		VLog.d("On Stop on HomeFragment")
	}

	override fun onDestroyView() {
		super.onDestroyView()
		VLog.d("On DestroyView on HomeFragment")
		cryptoJob?.cancel()
	}

	override fun onDestroy() {
		super.onDestroy()
		VLog.d("On Destroy HomeFragment")
	}


}
