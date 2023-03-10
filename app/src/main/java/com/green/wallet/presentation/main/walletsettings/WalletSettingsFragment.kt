package com.green.wallet.presentation.main.walletsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.R
import com.green.wallet.databinding.FragmentWalletSettingsBinding
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.custom.hidePublicKey
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates


class WalletSettingsFragment : DaggerFragment() {


	private lateinit var binding: FragmentWalletSettingsBinding

	@Inject
	lateinit var dialogManager: DialogManager

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
			curActivity().apply {
				dialogManager.showAssuranceDialogDefaultSetting(
					this,
					getStringResource(R.string.restore_def_settings),
					getStringResource(R.string.restore_def_settings_subtext),
					btnYes = {
						seekBarObserver.progress = 12
						seekBarNonObserver.progress = 5
						importMoreHashes()
					},
					btnNo = {

					}
				)
			}
		}

		icQuestionMarkNonObserver.setOnClickListener {
			curActivity().apply {
				dialogManager.showQuestionDetailsDialog(
					this,
					getStringResource(R.string.derivation_index_title),
					getStringResource(R.string.derivation_index_subtext),
					getStringResource(R.string.ok_button)
				) {

				}
			}
		}

		icQuestionMarkObserver.setOnClickListener {
			curActivity().apply {
				dialogManager.showQuestionDetailsDialog(
					this,
					getStringResource(R.string.derivation_index_title),
					getStringResource(R.string.derivation_index_subtext),
					getStringResource(R.string.ok_button)
				) {

				}
			}
		}

		backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}

		seekBarObserver.max = 48
		seekBarObserver.min = 1
		seekBarNonObserver.max = 24
		seekBarNonObserver.min = 1

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
			dialogManager.showProgress(curActivity())
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
				//asset_id
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
				dialogManager.hidePrevDialogs()
				curActivity().apply {
					dialogManager.showSuccessDialog(
						this,
						getStringResource(R.string.settings_saved),
						getStringResource(R.string.settings_saved_subtext),
						getStringResource(R.string.ready_btn),
						isDialogOutsideTouchable = false
					) {
						binding.btnSaveSettings.isEnabled = true
						curActivity().popBackStackOnce()
					}
				}
			}
		}
	}


	private fun curActivity() = requireActivity() as MainActivity

	override fun onStart() {
		super.onStart()
		if (dialogManager.isProgressDialogShowing() == true) {
			dialogManager.hidePrevDialogs()
			dialogManager.showProgress(curActivity())
		}
	}


}
