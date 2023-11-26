package com.green.wallet.presentation.main.enterpasscode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.R
import com.green.wallet.databinding.FragmentEnterPasscodeMainBinding
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.viewBinding
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_enter_passcode_main.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class EnterPasscodeFragment : DaggerFragment() {

    private val binding by viewBinding(FragmentEnterPasscodeMainBinding::bind)
    private var reason: ReasonEnterCode? = null


    @Inject
    lateinit var dialogManager: DialogManager

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: EnterPasscodeViewModel by viewModels { viewModelFactory }

    companion object {
        const val REASON_KEY = "reason_key"
    }

    private var jobAfter3sRedWarningGone: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.get(REASON_KEY) != null) {
            reason = arguments?.get(REASON_KEY) as ReasonEnterCode
        }
    }

    private var index = 0
    private var passCode = IntArray(6) { 0 }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enter_passcode_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBtnClicks()
        gettingValueOfButtonsForPasscode()
    }

    private fun registerBtnClicks() {

        binding.btnCancelPasscode.setOnClickListener {
            curActivity().popBackStackOnce()
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
        curActivity().popBackStackOnce()
        when (reason) {
            ReasonEnterCode.DELETE_WALLET -> showSuccessDeletedDialog()
            ReasonEnterCode.SHOW_DATA -> showWalletData()
            ReasonEnterCode.SEND_MONEY -> sendMoneySuccess()
            else -> viewModel.setPassCode(reason!!)
        }
    }

    private fun sendMoneySuccess() {
        curActivity().mainViewModel.send_money_success()
    }

    private fun showWalletData() {
        curActivity().mainViewModel.show_data_wallet_visible()
    }

    private fun showSuccessDeletedDialog() {
        curActivity().mainViewModel.deleteWalletTrue()
    }


    private fun curActivity() =
        requireActivity() as MainActivity


}
