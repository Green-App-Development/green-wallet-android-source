package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogBtmChooseWalletBinding
import com.green.wallet.databinding.ItemWalletBtmChooseBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BtmChooseWallet : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBtmChooseWalletBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val vm: TibetSwapViewModel by viewModels { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBtmChooseWalletBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initFingerPrints()
        VLog.d("On View Created ChooseWallet with vm : $vm")
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
    }

    private fun DialogBtmChooseWalletBinding.initFingerPrints() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.walletList.collectLatest { walletList ->

                    for (i in 0 until walletList.size) {
                        val bindingWallet = ItemWalletBtmChooseBinding.inflate(layoutInflater)
                        val wallet = walletList[i]
                        bindingWallet.apply {
                            txtPublicKey.text =
                                "Приватный ключ с публичным отпечатком  ${wallet.fingerPrint}"
                            root.setOnClickListener {
                                vm.curWallet = wallet
                                dismiss()
                            }
                            if (i == walletList.size - 1)
                                viewDivider.visibility = View.GONE
                        }
                        containerWallet.addView(bindingWallet.root)
                    }
                }
            }
        }

    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

}
