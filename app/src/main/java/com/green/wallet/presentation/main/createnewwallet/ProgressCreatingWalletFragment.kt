package com.green.wallet.presentation.main.createnewwallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.bip39.Mnemonics
import com.android.greenapp.R
import com.android.greenapp.databinding.ProgressWalletCreatingBinding
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.viewBinding
import com.green.wallet.presentation.tools.getColorResource
import com.example.common.tools.VLog
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 13.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ProgressCreatingWalletFragment : DaggerDialogFragment() {


	companion object {
		const val NETWORK_TYPE_KEY = "network_type_key"
	}

	var curNetworkType: String = "Chia Network"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		curNetworkType = arguments?.getString(NETWORK_TYPE_KEY, "Chia Network")!!
	}

	private val binding: ProgressWalletCreatingBinding by viewBinding(ProgressWalletCreatingBinding::bind)

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: NewWalletViewModel by viewModels { viewModelFactory }


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.progress_wallet_creating, container, false)
	}

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		addingThreeDotsEnd()
		initStatusBarColor()
		threeSecondsWaiting()
	}

	private fun threeSecondsWaiting() {
		lifecycleScope.launch {
			delay(2500)
			generateMnemonicsLocally()
		}
	}


	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor =
				requireActivity().getColorResource(R.color.status_bar_clr_progress_create_wallet)
			window?.decorView?.systemUiVisibility=0
		}
	}

	@SuppressLint("SetTextI18n")
	private fun addingThreeDotsEnd() {
		binding.apply {
			val curText = txtForCreatingANewWalletDescription.text.toString()
			txtForCreatingANewWalletDescription.text = "$curText..."
		}
	}

	private fun curActivity() = requireActivity() as MainActivity


	private fun generateMnemonicsLocally() {
		curActivity().curMnemonicCode = recursiveMnemonicGenerator()
		val generatedList = curActivity().curMnemonicCode.words.map { String(it) }.toList()
		VLog.d("CurNetworkType on Network Details Fragment : $curNetworkType")
		curActivity().move2SaveMnemonicFragment(generatedList, curNetworkType)
	}

	private fun recursiveMnemonicGenerator(): Mnemonics.MnemonicCode {
		val mnemonicsCode = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)
		val mnemonicsSet = mnemonicsCode.toList().toSet()
		if (mnemonicsSet.size < 12)
			return recursiveMnemonicGenerator()
		return mnemonicsCode
	}


}
