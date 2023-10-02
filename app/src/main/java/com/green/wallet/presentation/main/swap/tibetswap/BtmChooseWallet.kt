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
import com.green.wallet.presentation.custom.hidePublicKey
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import kotlinx.android.synthetic.main.fragment_exchange.container
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
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
    }

    private fun DialogBtmChooseWalletBinding.initFingerPrints() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.walletList.collectLatest { walletList ->
                    walletList?.let {
                        containerWallet.removeAllViews()
                        for (i in 0 until walletList.size) {
                            val bindingWallet = ItemWalletBtmChooseBinding.inflate(layoutInflater)
                            val wallet = walletList[i]
                            bindingWallet.apply {
                                txtPublicKey.text =
                                    "${requireActivity().getStringResource(R.string.private_key_with_public_fingerprint)}  ${
                                        hidePublicKey(
                                            wallet.fingerPrint
                                        )
                                    }"
                                root.setOnClickListener {
                                    vm.curWallet = wallet
                                    if (vm.isShowingSwap) {
                                        getMainActivity().move2BtmCreateOfferXCHCATDialog()
                                    } else {
                                        getMainActivity().move2BtmCreateOfferLiquidityDialog()
                                    }
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

    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

}
