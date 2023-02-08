package com.green.wallet.presentation.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import com.example.common.tools.*
import com.green.wallet.R
import com.green.wallet.R.id.*
import com.green.wallet.databinding.ActivityMainBinding
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.presentation.App
import com.green.wallet.presentation.BaseActivity
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.custom.NotificationHelper.Companion.GreenAppChannel
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.intro.IntroActivity
import com.green.wallet.presentation.main.address.AddressFragment
import com.green.wallet.presentation.main.address.EditAddressFragment
import com.green.wallet.presentation.main.btmdialogs.BtmSheetDialogChooseNetwork
import com.green.wallet.presentation.main.btmdialogs.BtmSheetDialogNewOrImport
import com.green.wallet.presentation.main.createnewwallet.CoinsDetailsFragment
import com.green.wallet.presentation.main.createnewwallet.CoinsDetailsFragment.Companion.NETWORK_KEY
import com.green.wallet.presentation.main.createnewwallet.ProgressCreatingWalletFragment
import com.green.wallet.presentation.main.createnewwallet.SaveMnemonicsFragment
import com.green.wallet.presentation.main.createnewwallet.VerificationFragment
import com.green.wallet.presentation.main.enterpasscode.EnterPasscodeFragment
import com.green.wallet.presentation.main.home.HomeFragment
import com.green.wallet.presentation.main.impmnemonics.ImpMnemonicFragment
import com.green.wallet.presentation.main.importtoken.ImportTokenFragment
import com.green.wallet.presentation.main.managewallet.ManageWalletFragment
import com.green.wallet.presentation.main.nft.usernfts.UserNFTTokensFragment
import com.green.wallet.presentation.main.notification.NotifFragment.Companion.SHOW_GREEN_APP_NOTIF
import com.green.wallet.presentation.main.receive.ReceiveFragment
import com.green.wallet.presentation.main.scan.ScannerFragment
import com.green.wallet.presentation.main.send.SendFragment
import com.green.wallet.presentation.main.service.AppRemovedRecentTaskService
import com.green.wallet.presentation.main.transaction.TransactionsFragment
import com.green.wallet.presentation.main.walletsettings.WalletSettingsFragment
import com.green.wallet.presentation.tools.*
import dev.b3nedikt.reword.Reword
import kotlinx.coroutines.*
import javax.inject.Inject


class MainActivity : BaseActivity() {


	companion object {
		const val MAIN_BUNDLE_KEY = "main_bundle"
	}

	private lateinit var binding: ActivityMainBinding
	private val navController by lazy { findNavController(my_nav_host_fragment) }
	var bottomSettingDialog: Dialog? = null
	var allSettingsDialog: DialogFragment? = null

	@Inject
	lateinit var viewModelFactory: ViewModelFactory

	var shouldGoBackHomeFragmentFromTransactions = false
	var shouldGoBackHomeFragmentFromAddress = false

	var curChosenNetworkTypePosForBackImport = "Chia Network"

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var notificationHelper: NotificationHelper


	var curMnemonicCode = MnemonicCode(Mnemonics.WordCount.COUNT_12)

	private val handler = CoroutineExceptionHandler { context, throwable ->
		VLog.d("Exception in main Activity : $throwable")
	}

	val mainViewModel: MainViewModel by viewModels { viewModelFactory }

	private var sendReceiveJob: Job? = null
	private var networkItemsJob: Job? = null
	var impMnemonicsFragmentView: View? = null
	var sendCoinsFragmentView: View? = null
	var listingFragmentView: View? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		VLog.d(
			"OnCreate got called on MainActivity 1"
		)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		handlingVisibilityBottomNavView()
		bottomNavViewClicks()
		initStatusBarColorRegulation()
		(application as App).applicationIsAlive = true
		softKeyboardListener()
		initCheckingBundleFromPushNotification()
		startServiceAppRemoveRecentTask()
	}


	private fun initCheckingBundleFromPushNotification() {
		val bundle = intent.getBundleExtra(MAIN_BUNDLE_KEY)
		if (bundle == null) {
			VLog.d("Bundle is null in mainActivity")
			return
		}
		val greenAppNotif = bundle.getString(GreenAppChannel, "")
		if (greenAppNotif.isNotEmpty()) {
			move2NotificationFragment(showGreenAppNotif = true)
		}
	}


	private fun initUpdateBalanceJobRegulation() {
		navController.addOnDestinationChangedListener { navController, dest, bundle ->
			VLog.d("MainNav navController destination id changed  : ${dest.id}")
			val shouldStopUpdateBalance = setOf(
				sendFragment, impMnemonicFragment,
				verificationFragment
			)
			if (shouldStopUpdateBalance.contains(dest.id)) {
				VLog.d("ShouldStopUpdateBalance destination on MainActivity")
				(application as App).updateBalanceJob?.cancel()
			} else {
				VLog.d("Start updating balance and transaction periodically")
				(application as App).updateBalanceEachPeriodically()
			}
		}
	}

	private var startServiceJobLater: Job? = null

	private fun startServiceAppRemoveRecentTask() {
		startServiceJobLater = lifecycleScope.launch(handler) {
			delay(1000L)
			val stickyService = Intent(this@MainActivity, AppRemovedRecentTaskService::class.java)
			startService(stickyService)
		}
	}

	override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
		VLog.d("OnCreateView on MainActivity")
		return super.onCreateView(name, context, attrs)
	}

	override fun onStart() {
		super.onStart()
		VLog.d("OnStart on MainActivity")
		checkUserLastVisitTime()
	}

	private fun checkUserLastVisitTime() {
		lifecycleScope.launch {
			val lastTimeVisited = mainViewModel.getLastVisitedValue()
			if (System.currentTimeMillis() - lastTimeVisited >= 180 * 1000) {
				(application as App).applicationIsAlive = false
				Intent(this@MainActivity, IntroActivity::class.java).apply {
					this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
					startActivity(this)
				}
				startServiceJobLater?.cancel()
				finish()
			}
		}
	}

	override fun onResume() {
		super.onResume()
		VLog.d("OnResume on MainActivity")
	}


	private fun initStatusBarColorRegulation() {
		navController.addOnDestinationChangedListener { navController, dest, bundle ->
			VLog.d("MainNav navController destination id changed  : ${dest.id}")
			when (dest.id) {
				homeFragment -> {
					binding.mainBottomNav.menu.findItem(home).isChecked = true
					window.statusBarColor = getColorResource(R.color.status_bar_color_home)
					setSystemUiLightStatusBar(isLightStatusBar = false)
				}
				progressWalletCreating -> {
//					window.statusBarColor = getColorResource(R.color.status_bar_color_home)
//					setSystemUiLightStatusBar(isLightStatusBar = false)
				}
				transactionsFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				addressFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				allWalletListFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				verificationFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				manageWalletFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				walletSettings -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				importTokenFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				saveMnemonicsFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				allSettingsFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.status_bar_color_send)
				}
				addAddressFragment -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				entPasscodeFrMain -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
//				noConnectionFragment -> {
//					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
//					window.statusBarColor = getColorResource(R.color.primary_app_background)
//				}
				fragmentUserNFTs -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				fragmentNFTDetails -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.primary_app_background)
				}
				else -> {
					setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
					window.statusBarColor = getColorResource(R.color.status_bar_color_send)
				}
			}
		}
	}

	private fun bottomNavViewClicks() {
		binding.mainBottomNav.setOnItemSelectedListener { item ->
			when (item.itemId) {
				home -> {
					if (navController.currentDestination?.id != homeFragment) {
						item.isChecked = true
						popBackStackTillHomeFragment()
					}
				}
				dapp -> {
					sendEventToFragment()
				}
				nfts -> {
					if (navController.currentDestination?.id != fragmentUserNFTs) {
						item.isChecked = true
						move2UserNFTTokensFragment()
					}
				}
			}
			false
		}
	}

	private fun sendEventToFragment() {
		VLog.d("Nft clicked on peek fragment : ${supportFragmentManager.currentNavigationFragment}")
		when (val curFragment = supportFragmentManager.currentNavigationFragment) {
			is HomeFragment -> {
				curFragment.onCommentUnderDevClicked()
			}
			is UserNFTTokensFragment -> {
				curFragment.onCommentUnderDevClicked()
			}
		}

	}

	private fun popBackStackTillHomeFragment() {
		while (navController.currentDestination?.id != homeFragment) {
			navController.popBackStack()
		}
	}

	fun popBackTillHomeFragment() {

	}

	@Suppress("DEPRECATION")
	fun setSystemUiLightStatusBar(isLightStatusBar: Boolean) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				val systemUiAppearance = if (isLightStatusBar) {
					WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
				} else {
					0
				}
				window.insetsController?.setSystemBarsAppearance(
					systemUiAppearance,
					WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
				)
			} else {
				val systemUiVisibilityFlags = if (isLightStatusBar) {
					window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
				} else {
					window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
				}
				window.decorView.systemUiVisibility = systemUiVisibilityFlags
			}
		}
	}

	private fun showDialogNetworkToSend() {
		sendReceiveJob?.cancel()
		sendReceiveJob = lifecycleScope.launch {
			val networkTypesFromDb = mainViewModel.getDistinctNetworkTypes()

			if (networkTypesFromDb.isEmpty()) {
				showBtmDialogCreateOrImportNewWallet(false)
				return@launch
			}

			showBtmDialogChoosingNetworks(
				true,
				this@MainActivity,
				networkTypesFromDb
			) { position ->
				move2SendFragment(networkTypesFromDb[position], null, shouldQRCleared = true)
			}
		}

	}

	fun move2SendFragment(networkType: String, fingerPrint: Long?, shouldQRCleared: Boolean) {
		if (shouldQRCleared) {
			mainViewModel.saveDecodeQrCode("")
			mainViewModel.saveChosenAddress("")
		}
		val bundle = bundleOf(
			SendFragment.NETWORK_TYPE_KEY to networkType,
			SendFragment.FINGERPRINT_KEY to fingerPrint
		)
		navController.navigate(sendFragment, bundle)
	}

	private fun handlingVisibilityBottomNavView() {
		navController.addOnDestinationChangedListener { controller, destination, arguments ->
			if (setOf(
					coinsDetailsFragment2,
					progressWalletCreating,
					impMnemonicFragment,
					receiveFragment,
					sendFragment,
					scannerFragment,
					saveMnemonicsFragment,
					verificationFragment,
					allWalletListFragment,
					entPasscodeFrMain,
					manageWalletFragment,
					importTokenFragment,
					supportFragment,
					allSettingsFragment,
					mainLanguageFragment,
					aboutAppFragment,
					addAddressFragment,
					notificationFragment,
					transactionsFragment,
					walletSettings,
					fragmentNFTDetails
				).contains(destination.id)
			) {
				binding.mainBottomNav.visibility = View.GONE
			} else {
				binding.mainBottomNav.visibility = View.VISIBLE
			}
		}
	}

	private fun showDialogNetworkToReceive() {

		sendReceiveJob?.cancel()
		sendReceiveJob = lifecycleScope.launch {
			val networkTypesFromDb = mainViewModel.getDistinctNetworkTypes()

			if (networkTypesFromDb.isEmpty()) {
				showBtmDialogCreateOrImportNewWallet(false)
				return@launch
			}

			showBtmDialogChoosingNetworks(true, this@MainActivity, networkTypesFromDb) { position ->
				move2ReceiveFragment(networkTypesFromDb[position], null)
			}
		}

	}


	fun move2HomeFragment() {
		navController.popBackStack(homeFragment, inclusive = true)
		navController.navigate(homeFragment)
	}

	fun move2TransactionsFragment(fingerPrint: Long = 0, address: String = "") {
		val bundle = bundleOf(
			TransactionsFragment.FINGER_PRINT_KEY to fingerPrint,
			TransactionsFragment.ADDRESS_KEY to address
		)
//		binding.mainBottomNav.menu.findItem(transactions).isChecked = true
		navController.navigate(transactionsFragment, bundle)
	}

	fun move2ReceiveFragment(coinType: String, fingerPrint: Long?) {
		val bundle = bundleOf(
			ReceiveFragment.NETWORK_TYPE_KEY to coinType,
			ReceiveFragment.FINGERPRINT_KEY to fingerPrint
		)
		navController.navigate(receiveFragment, bundle)
	}

	fun move2CoinsDetailsFragment(networkType: String, forImportMnemonics: Boolean = false) {
		val bundle =
			bundleOf(
				NETWORK_KEY to networkType,
				CoinsDetailsFragment.FOR_IMPORT_MNEMONICS_KEY to forImportMnemonics
			)
		navController.navigate(coinsDetailsFragment2, bundle)
	}

	fun move2ImportMnemonicFragment(networkType: String) {
		val bundle = bundleOf(ImpMnemonicFragment.NETWORK_TYPE_KEY to networkType)
		navController.navigate(impMnemonicFragment, bundle)
	}


	fun move2ScannerFragment(fingerPrint: Long?, networkType: String?) {
		val bundle = bundleOf(
			ScannerFragment.FINGERPRINT_KEY to fingerPrint,
			ScannerFragment.NETWORK_TYPE_KEY to networkType
		)
		navController.navigate(scannerFragment, bundle)
	}


	fun move2SaveMnemonicFragment(mnemonics: List<String>, networkType: String) {
		val bundle = bundleOf(
			SaveMnemonicsFragment.MNEMONICS to mnemonics,
			SaveMnemonicsFragment.NETWORK_TYPE to networkType
		)
		navController.navigate(saveMnemonicsFragment, bundle)
	}

	fun move2VerificationMnemonicFragment(optionsWords: List<String>, networkType: String) {
		val bundle = bundleOf(
			VerificationFragment.OPTIONS_WORDS_KEY to optionsWords,
			VerificationFragment.NETWORK_TYPE_KEY to networkType
		)
		navController.navigate(verificationFragment, bundle)
	}

	fun showEnterPasswordFragment(reason: ReasonEnterCode) {
		val bundle = bundleOf(EnterPasscodeFragment.REASON_KEY to reason)
		navController.navigate(entPasscodeFrMain, bundle)
	}

	fun popBackStackOnce() = navController.popBackStack()

	fun popBackStackTwice() {
		navController.popBackStack()
		navController.popBackStack()
	}


	fun backPressedOnImpMnemonicFragment() {
		val bundle = bundleOf(HomeFragment.SHOW_DIALOG_ONE to true)
		navController.navigate(homeFragment, bundle)
	}

	fun move2AllWalletFragment() {
		navController.navigate(action_walletFragment_to_allWalletFragment)
	}

	fun move2NFTDetailsFragment() {
		navController.navigate(fragmentNFTDetails)
	}

	fun move2ManageWalletFragment(id: Int) {
		val bundle = bundleOf(ManageWalletFragment.WALLET_KEY to id)
		navController.navigate(manageWalletFragment, bundle)
	}

	fun move2FragmentImportToken(
		fingerPrint: Long,
		main_puzzle_hash: String,
		networkType: String = "Chia Network",
		address: String
	) {
		val bundle = bundleOf(
			ImportTokenFragment.FINGER_PRINT_KEY to fingerPrint,
			ImportTokenFragment.NETWORK_TYPE_KEY to networkType,
			ImportTokenFragment.MAIN_PUZZLE_HASH to main_puzzle_hash,
			ImportTokenFragment.ADDRESS_KEY to address
		)
		navController.navigate(action_walletFragment_to_importTokenFragment2, bundle)
	}

	fun move2SupportFragmentFromBtmDialog() {
		navController.navigate(action_walletFragment_to_supportFragment)
	}

	fun move2SupportFragmentFromAllSettingsDialog() {
		navController.navigate(action_allSettingsFragment_to_supportFragment)
	}

	fun move2AllSettingsFragment() {
		navController.navigate(action_walletFragment_to_allSettingsFragment)
	}

	fun move2MainLanguageFragment() {
		navController.navigate(action_allSettingsFragment_to_mainLanguageFragment)
	}

	fun move2AboutApp() {
		navController.navigate(aboutAppFragment)
	}

	fun move2AddressFragmentList(clickable: Boolean = false) {
		val bundle = bundleOf(AddressFragment.SHOULD_BE_CLICKABLE_KEY to clickable)
//		binding.mainBottomNav.menu.findItem(address).isChecked = true
		navController.navigate(addressFragment, bundle)
	}

	fun move2NotificationFragment(showGreenAppNotif: Boolean = false) {
		val bundle = bundleOf()
		bundle.putBoolean(SHOW_GREEN_APP_NOTIF, showGreenAppNotif)
		navController.navigate(notificationFragment, bundle)
	}

	fun move2FaqFragment() {
		navController.navigate(FAQFragment)
	}

	fun move2AskQuestionFragment() {
		navController.navigate(questionFragment)
	}

	fun move2ListingFragment() {
		navController.navigate(listingFragment)
	}

	fun move2UserNFTTokensFragment() {
		navController.navigate(fragmentUserNFTs)
	}

	fun move2EditAddressFragment(itemAddress: Address?) {
		mainViewModel.saveDecodeQrCode("")
		if (itemAddress != null) {
			val bundle = bundleOf(EditAddressFragment.ADDRESS_ID to itemAddress)
			navController.navigate(action_addressFragment_to_addAddressFragment, bundle)
		} else
			navController.navigate(action_addressFragment_to_addAddressFragment)
	}

	fun move2ProgressCreatingWalletFragment(networkType: String) {
		val bundle = bundleOf(ProgressCreatingWalletFragment.NETWORK_TYPE_KEY to networkType)
		navController.navigate(progressWalletCreating, bundle)
	}

	override fun onBackPressed() {

		if ((navController.currentDestination?.id == transactionsFragment && shouldGoBackHomeFragmentFromTransactions) || (navController.currentDestination?.id == addressFragment && shouldGoBackHomeFragmentFromAddress)) {
			popBackStackTillHomeFragment()
			return
		}

		super.onBackPressed()
	}

	fun launchingIntentForSendingWalletAddress(address: String) {
		val intent = Intent()
		intent.setAction(Intent.ACTION_SEND)
		val content = address
		intent.type = "text/plain"
		intent.putExtra(
			Intent.EXTRA_SUBJECT,
			getString(R.string.address_wallet)
		)
		intent.putExtra(Intent.EXTRA_TEXT, content)
		startActivity(Intent.createChooser(intent, "Share"))
	}


	val requestSendingTextViaMessengers =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

		}

	override fun onPause() {
		super.onPause()
		VLog.d("onPause MainActivity")
	}

	override fun onStop() {
		super.onStop()
		VLog.d("onStop MainActivity")
		mainViewModel.saveLastVisitedLongValue(System.currentTimeMillis())
	}

	override fun onDestroy() {
		super.onDestroy()
		VLog.d("onDestroy MainActivity")
	}


	fun onLanguageChanged() {
		VLog.d("OnLanguage Changed")
		if (bottomSettingDialog != null) {
			val rootBtmSetting =
				bottomSettingDialog!!.findViewById<LinearLayout>(rootLayoutBottom)
			VLog.d("RootBtmSetting : $rootBtmSetting")
			Reword.reword(rootBtmSetting)
		}
		if (allSettingsDialog != null) {
			val rootALlSettings = allSettingsDialog!!.dialog?.findViewById<ConstraintLayout>(
				root_all_settings
			)
			VLog.d("RootBtmSetting : $rootALlSettings")
			if (rootALlSettings != null) {
				Reword.reword(rootALlSettings)
			}
		}
	}


	private fun softKeyboardListener() {
		window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
			val r = Rect()
			window.decorView.getWindowVisibleDisplayFrame(r)

			val height = window.decorView.height
			if (height - r.bottom > height * 0.1399) {
				changeVisibilityOfViewsDuringSoftKeyBoardOpen(true)
			} else {
				changeVisibilityOfViewsDuringSoftKeyBoardOpen(false)
			}
		}
	}

	var prevSoftKeyboardValue = false

	private fun changeVisibilityOfViewsDuringSoftKeyBoardOpen(visibility: Boolean) {
		VLog.d("Soft key board is open : $visibility")
		if (visibility == prevSoftKeyboardValue)
			return

		prevSoftKeyboardValue = visibility

		try {
			if (impMnemonicsFragmentView != null) {
				val tempLinear = impMnemonicsFragmentView!!.findViewById<LinearLayout>(temp_linear)
				val tempLinear2 =
					impMnemonicsFragmentView!!.findViewById<LinearLayout>(temp_linear_2)
				tempLinear.visibility = if (visibility) View.VISIBLE else View.GONE
				tempLinear2.visibility = if (visibility) View.VISIBLE else View.GONE
			}
			if (sendCoinsFragmentView != null) {
				val tempLinear = sendCoinsFragmentView!!.findViewById<LinearLayout>(temp_linear)
				tempLinear.visibility = if (visibility) View.VISIBLE else View.GONE
				sendCoinsFragmentView!!.findViewById<OnlyVerticalSwipeRefreshLayout>(swipeRefresh).isEnabled =
					!visibility
			}
			if (listingFragmentView != null) {
				val linearAgree = listingFragmentView!!.findViewById<LinearLayout>(linearAgree)
				val button = listingFragmentView!!.findViewById<Button>(btnSend)
				button.visibility = if (visibility) View.GONE else View.VISIBLE
				linearAgree.visibility = if (visibility) View.GONE else View.VISIBLE
			}
		} catch (ex: Exception) {
			VLog.d("Exception in updating views when soft keyboard pops up : ${ex.message}")
		}
	}

	fun showBtmDialogCreateOrImportNewWallet(hasAtLeastOneWallet: Boolean) {
		if (networkItemsJob != null && networkItemsJob!!.isActive) {
			VLog.d("Previous NetworkItemsJob is active ")
			return
		}
		networkItemsJob = lifecycleScope.launch {
			delay(1000)
		}
		VLog.d("Create or import method has  been called on MainActivity")
		lifecycleScope.launch {

			val walletSize = mainViewModel.getWalletSizeInDB().size
			if (walletSize >= 10) {
				dialogMan.showFailureDialog(
					this@MainActivity,
					getStringResource(R.string.pop_up_failed_error_title),
					getStringResource(R.string.the_application_does_not_support_more_than_wallets),
					getStringResource(R.string.return_btn)
				) {

				}
				return@launch
			}

			val res = mainViewModel.getAvailableNetworkItems()

			VLog.d("All NetworkItems List to create or import new wallet : ${res.data?.toList()}")

			when (res.state) {
				Resource.State.SUCCESS -> {
					val networkList = res.data!!.map { it.name }.toList()
					move2BtmDialogChooseNetwork(hasAtLeastOneWallet, networkList)
				}
				Resource.State.LOADING -> {

				}
				Resource.State.ERROR -> {
					VLog.d("Error create or import wallet dialog error : ${res.error}")
					dialogMan.showServerErrorDialog(this@MainActivity) {

					}
				}
			}
		}
	}

	fun showBtmDialogAddOrImportWallets(networkType: String) {
		move2NewOrImportWallet(networkType)
	}

	fun move2BtmDialogChooseNetwork(hasAtLeastOneWallet: Boolean, dataList: List<String>) {
		val bundle = bundleOf(
			BtmSheetDialogChooseNetwork.DATA_LIST_KEY to dataList,
			BtmSheetDialogChooseNetwork.HAS_AT_LEAST_ONE to hasAtLeastOneWallet
		)
		navController.navigate(btmSheetChooseNetwork, bundle)
	}

	fun move2NewOrImportWallet(networkType: String) {
		val bundle = bundleOf(BtmSheetDialogNewOrImport.NETWORK_TYPE_KEY to networkType)
		navController.navigate(R.id.btmSheetDialogNewOrImport, bundle)
	}

	var importBackBtnJob: Job? = null

	fun showTwoBtmDialogsFromImportBackButton() {
		importBackBtnJob?.cancel()
		importBackBtnJob = lifecycleScope.launch {
			val networkTypeDistinct = mainViewModel.getDistinctNetworkTypes()
			showBtmDialogCreateOrImportNewWallet(networkTypeDistinct.isNotEmpty())
			delay(100)
			move2NewOrImportWallet(curChosenNetworkTypePosForBackImport)
		}
	}

	fun move2WalletSettings(address: String) {
		val bundle = bundleOf(
			WalletSettingsFragment.ADDRESS_KEY to address
		)
		navController.navigate(walletSettings, bundle)
	}


}
