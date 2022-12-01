package com.android.greenapp.presentation.main.walletsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.databinding.FragmentWalletSettingsBinding
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.presentation.App
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.custom.convertListToStringWithSpace
import com.android.greenapp.presentation.custom.hidePublicKey
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.android.greenapp.presentation.tools.preventDoubleClick
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * Created by bekjan on 29.11.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletSettingsFragment : DaggerFragment() {


	private lateinit var binding: FragmentWalletSettingsBinding

	@Inject
	lateinit var dialog: DialogManager

	companion object {
		const val ADDRESS_KEY = "address_key"
	}


	var fingerPrint by Delegates.notNull<Int>()
	lateinit var address: String
	lateinit var wallet: Wallet

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: WalletSettingsViewModel by viewModels { viewModelFactory }


	lateinit var methodChannel: MethodChannel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			address = it.getString(ADDRESS_KEY, "")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentWalletSettingsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.registerClicks()
		initViewUpdates()
		initVariable()
	}

	private fun initVariable() {
		methodChannel = MethodChannel(
			(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
	}

	private fun initViewUpdates() {
		lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				wallet = viewModel.getWalletByAddress(address = address)
			}
			binding.networkFingerPrint.setText(
				"${wallet.networkType.split(" ")[0]}  ${
					hidePublicKey(
						wallet.fingerPrint!!
					)
				}"
			)

			with(wallet) {
				binding.apply {
					txtNonObserverPercent.setText("$nonObserverHash")
					txtObserverPercent.setText("$observerHash")
					seekBarNonObserver.setProgress(nonObserverHash)
					seekBarObserver.setProgress(observerHash)
				}
			}

		}
	}


	private fun FragmentWalletSettingsBinding.registerClicks() {

		btnSaveSettings.setOnClickListener {
			it.isEnabled = false
			importMoreHashes()
		}

		txtDefaultSettings.setOnClickListener {
			dialog.showAssuranceDialogDefaultSetting(
				curActivity(),
				"Restore default settings?",
				"This action will return the settings of the wallet derivative index to the default values, which may lead to incorrect display of the balance",
				btnYes = {
					seekBarObserver.progress = 1
					seekBarNonObserver.progress = 1
					importMoreHashes()
				},
				btnNo = {

				}
			)
		}

		icQuestionMarkNonObserver.setOnClickListener {
			dialog.showQuestionDetailsDialog(
				curActivity(),
				"Derivation Index",
				"The derivation index sets the range of wallet addresses that the wallet scans the blockchain for. This number is generally higher if you have a lot of transactions or canceled offers for XCH, CATs, or NFTs. If you believe your balance is incorrect because it’s missing coins, then increasing the derivation index could help the wallet include the missing coins in the balance total.\u2028\n" +
						"Use this setting if, after importing mnemonics from Goby, Nucle, Arbor wallets, the balance of the wallet is displayed incorrectly\n",
				"Okay"
			) {

			}
		}

		icQuestionMarkObserver.setOnClickListener {
			dialog.showQuestionDetailsDialog(
				curActivity(),
				"Derivation Index",
				"The derivation index sets the range of wallet addresses that the wallet scans the blockchain for. This number is generally higher if you have a lot of transactions or canceled offers for XCH, CATs, or NFTs. If you believe your balance is incorrect because it’s missing coins, then increasing the derivation index could help the wallet include the missing coins in the balance total.\u2028\n" +
						"Use this setting if, after importing mnemonics from Goby, Nucle, Arbor wallets, the balance of the wallet is displayed incorrectly\n",
				"Okay"
			) {

			}
		}

		backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}

		seekBarObserver.max = 24
		seekBarNonObserver.max = 24

		seekBarObserver.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				txtObserverPercent.text = "$p1"
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {

			}

			override fun onStopTrackingTouch(p0: SeekBar?) {

			}

		})

		seekBarNonObserver.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				txtNonObserverPercent.text = "$p1"
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {

			}

			override fun onStopTrackingTouch(p0: SeekBar?) {

			}
		})


	}

	private fun importMoreHashes() {
		listenToMethodCall()
		val nObserver = binding.seekBarNonObserver.progress
		val observer = binding.seekBarObserver.progress
		val max = Math.max(nObserver, observer)
		if (max >= 2)
			dialog.showProgress(curActivity())
		val mnemonics = convertListToStringWithSpace(wallet.mnemonics)
		val asset_id_s = convertListToStringWithSpace(wallet.hashListImported.keys.toList())
		val map = hashMapOf<String, Any>()
		map["mnemonicString"] = mnemonics
		map["observer"] = observer
		map["non_observer"] = nObserver
		map["asset_ids"] = asset_id_s
		methodChannel.invokeMethod("changeSettings", map)
	}

	private fun listenToMethodCall() {
		methodChannel.setMethodCallHandler { method, _ ->
			if (method.method == "changeSettings") {
				VLog.d("Arguments retrieved from flutter for settings : ${method.arguments}")
				val map = method.arguments as HashMap<*, *>
				val main_hashes = map["main_puzzle_hashes"] as List<String>
				val hashListImported = hashMapOf<String, List<String>>()
				wallet.hashListImported.keys.forEach {
					val token_hashes = map[it] as List<String>
					hashListImported[it] = token_hashes
				}
				viewModel.updateHashesMainToken(
					wallet.address,
					main_hashes,
					hashListImported,
					binding.seekBarObserver.progress,
					binding.seekBarNonObserver.progress
				)
				dialog.hidePrevDialogs()
				dialog.showSuccessDialog(
					curActivity(),
					"Saved",
					"Wallet settings saved successfully!",
					"Done",
					isDialogOutsideTouchable = false
				) {
					binding.btnSaveSettings.isEnabled = true
				}
			}
		}
	}


	private fun curActivity() = requireActivity() as MainActivity


}
