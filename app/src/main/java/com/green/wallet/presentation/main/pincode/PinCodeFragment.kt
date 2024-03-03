package com.green.wallet.presentation.main.pincode

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.databinding.FragmentEnterPasscodeMainBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class PinCodeFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(FragmentEnterPasscodeMainBinding::bind)
    private var reason: ReasonEnterCode? = null


    @Inject
    lateinit var dialogManager: DialogManager

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PinCodeViewModel by viewModels { viewModelFactory }

    private var jobAfter3sRedWarningGone: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.get(REASON_KEY) != null) {
            reason = arguments?.get(REASON_KEY) as ReasonEnterCode
        }
        (requireActivity().application as App).appComponent.inject(this)
    }


    private var index = 0
    private var passCode = IntArray(6) { 0 }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val dialog = object : BottomSheetDialog(requireContext(), theme) {
            @Deprecated("Deprecated")
            override fun onBackPressed() {
                dismiss()
            }
        }
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog =
                dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            com.green.wallet.R.layout.fragment_enter_passcode_main,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBtnClicks()
        gettingValueOfButtonsForPasscode()
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet!!)
        val layoutParams = bottomSheet.layoutParams
        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun registerBtnClicks() {

        binding.btnCancelPasscode.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        index = 0
        binding.circles.usedCircleCount = index
    }

    private fun gettingValueOfButtonsForPasscode() {

        kotlin.runCatching {

            for (linear in binding.linearNumbers.children) {
                linear as LinearLayoutCompat
                for (btn in linear.children)
                    btn.setOnClickListener {
                        if (index == 6)
                            return@setOnClickListener
                        if (btn is Button) {
                            val n = Integer.valueOf(btn.text.toString())
                            passCode[index++] = n
                            binding.circles.usedCircleCount = index
                        } else if (btn is RelativeLayout) {
                            if (btn.tag == "rel_img_back_space") {
                                VLog.d("Backspace is clicked")
                                index--
                                if (index < 0)
                                    index = 0
                                binding.circles.usedCircleCount = index
                            } else if (btn.tag == "rel_face_id") {
//                            askingFaceIdPermission()
                            }
                        }
                        if (index == 6) {
                            verifiedPasscode(passCode)
                        }
                    }
            }
        }.onFailure {
            VLog.e("OnFailure got exception caught in gridLayout for enterPasscode  :  $it")
        }.onSuccess {

        }

    }

    private fun verifiedPasscode(passCode: IntArray) {
        lifecycleScope.launch {
            val savedPassCode = viewModel.getSetPasscode()
            if (passCode.toList().toString() == savedPassCode) {
                determineDestination()
            } else {
                showMessagePasscodesDontMatch()
            }
        }
    }

    private fun showMessagePasscodesDontMatch() {
        index = 0
        binding.circles.usedCircleCount = index
        binding.circles.shouldBeRedAll = true
        binding.txtWrongPassCodeEntered.visibility = View.VISIBLE
        jobAfter3sRedWarningGone?.cancel()
        jobAfter3sRedWarningGone = lifecycleScope.launch {
            delay(2000)
            index = 0
            binding.circles.usedCircleCount = index
            binding.circles.shouldBeRedAll = false
            binding.txtWrongPassCodeEntered.visibility = View.GONE
        }
    }

    private fun determineDestination() {
        dismiss()
        when (reason) {
            ReasonEnterCode.DELETE_WALLET -> showSuccessDeletedDialog()
            ReasonEnterCode.SHOW_DATA -> showWalletData()
            ReasonEnterCode.SEND_MONEY -> sendMoneySuccess()
            else -> viewModel.setPassCode(reason!!)
        }
    }

    private fun sendMoneySuccess() {
        curActivity().mainViewModel.sendMoneySuccess()
    }

    private fun showWalletData() {
        curActivity().mainViewModel.showDataWalletVisible()
    }

    private fun showSuccessDeletedDialog() {
        curActivity().mainViewModel.deleteWalletTrue()
    }

    private fun curActivity() =
        requireActivity() as MainActivity


    companion object {

        const val REASON_KEY = "reason_key"
        fun build(reason: ReasonEnterCode) = PinCodeFragment().apply {
            val bundle = bundleOf(REASON_KEY to reason)
            arguments = bundle
        }
    }

}
