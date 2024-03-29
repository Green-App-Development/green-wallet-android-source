package com.green.wallet.presentation.main.createnewwallet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.R
import com.green.wallet.databinding.FragmentSavemnemonicsBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SaveMnemonicsFragment : DaggerDialogFragment() {

	private lateinit var binding: FragmentSavemnemonicsBinding
	private var hidden = true

	private lateinit var mnemonicsList: java.util.ArrayList<String>
	private var curNetworkType: String = ""

	@Inject
	lateinit var effect: AnimationManager


	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val newWalletViewModel: NewWalletViewModel by viewModels { viewModelFactory }

	companion object {
		const val MNEMONICS = "mnemonics_key"
		const val NETWORK_TYPE = "network_type"
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			val mnemonics = it.getStringArrayList(MNEMONICS)
			if (mnemonics != null) {
				mnemonicsList = mnemonics
			}
			if (it.getString(NETWORK_TYPE) != null)
				curNetworkType = it.getString(NETWORK_TYPE)!!
		}
	}

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSavemnemonicsBinding.inflate(inflater)
		getMainActivity().window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerBtnClicks()
		populatingEdtWithWords2Version(visible = false, mnemonicsList)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = object : Dialog(curActivity(), theme) {
			override fun onBackPressed() {
				curActivity().popBackStackTwice()
			}
		}
		return dialog
	}


	@SuppressLint("SetTextI18n")
	private fun populatingEdtWithWords2Version(visible: Boolean, mnemonics: ArrayList<String>) {
		var index = 0
		var leftSide = 1
		var rightSide = 7
		try {
			for (pairLinearLay in binding.linearLayout12.children) {
				val curLinear = (pairLinearLay as LinearLayout)
				val edt1 = curLinear.getChildAt(0) as EditText
				val edt2 = curLinear.getChildAt(1) as EditText
				if (visible) {
					edt1.setText("${leftSide}. ${mnemonics[leftSide - 1]}")
					index++
					edt2.setText("${rightSide}. ${mnemonics[rightSide - 1]}")
					index++
				} else {
					edt1.setText("${leftSide}. ${"•".repeat(mnemonics[index].length)}")
					index++
					edt2.setText("${rightSide}. ${"•".repeat(mnemonics[index].length)}")
					index++
				}
				leftSide++
				rightSide++
			}
		} catch (ex: Exception) {
			VLog.e("Exception in populating edts with words : ${ex.message}")
		}
	}


	private fun registerBtnClicks() {
		binding.checkboxAgree.setOnCheckedChangeListener { p0, p1 ->
			binding.btnContinue.isEnabled = p1
			binding.checkboxAgree.setTextColor(
				if (p1) ContextCompat.getColor(
					curActivity(),
					R.color.green
				) else ContextCompat.getColor(curActivity(), R.color.secondary_text_color)
			)
		}

		binding.eyeIc.setOnClickListener {
			populatingEdtWithWords2Version(hidden, mnemonicsList)
			if (hidden) {
				binding.eyeIc.setImageDrawable(
					ContextCompat.getDrawable(curActivity(), R.drawable.eye_ic_off)
				)
				hidden = false
			} else {
				binding.eyeIc.setImageDrawable(
					ContextCompat.getDrawable(curActivity(), R.drawable.eye_ic_on)
				)
				hidden = true
			}
		}

		binding.btnCopy.setOnClickListener {
			binding.relCopied.visibility = View.VISIBLE
			val mnemonics = getMnemonicsFromList()
			VLog.d("Copied Mnemonics : $mnemonics")
			val clipBoard =
				curActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
			val clip = ClipData.newPlainText(
				"label",
				mnemonics
			)
			clipBoard.setPrimaryClip(clip)
			lifecycleScope.launch {
				delay(2000)
				binding.relCopied.visibility = View.GONE
			}
		}

		binding.backLayout.setOnClickListener {
			curActivity().popBackStackTwice()
		}
		binding.btnContinue.setOnClickListener {
			VLog.d("CurNetworkType on SaveMnemonics Fragment : $curNetworkType")
			it.startAnimation(effect.getBtnEffectAnimation())
			curActivity().move2VerificationMnemonicFragment(
				optionsWords = mnemonicsList,
				curNetworkType
			)
		}

	}

	override fun onStart() {
		super.onStart()
		initStatusBarColor()
	}


	override fun onResume() {
		super.onResume()
		// Add this code in the onCreate() method of your Activity
		getMainActivity().window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE
		)
	}

	private fun getMnemonicsFromList(): String {
		val mneMnemonicsString = StringBuilder()
		for (i in 0 until mnemonicsList.size) {
			mneMnemonicsString.append("${mnemonicsList[i]} ")
		}
		return mneMnemonicsString.toString()
	}


	override fun onPause() {
		super.onPause()
		getMainActivity().window.clearFlags(
			WindowManager.LayoutParams.FLAG_SECURE
		)
	}

	override fun onStop() {
		super.onStop()
	}

	private fun curActivity() = requireActivity() as MainActivity

	override fun onDestroyView() {
		super.onDestroyView()
	}

	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.save_mnemonic_bcg)
		}
	}


}
