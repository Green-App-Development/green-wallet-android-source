package com.green.wallet.presentation.main.swap.tibetswapdetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.tools.formatString
import com.example.common.tools.formattedTimeForOrderItem
import com.example.common.tools.getRequestStatusColor
import com.example.common.tools.getRequestStatusTranslation
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTibetSwapDetailBinding
import com.green.wallet.databinding.FragmentTibetswapBinding
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.presentation.custom.base.makeViewVisibleAndGone
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.tibetswap.TibetSwapViewModel
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.copyToClipBoard
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_request_details.relCopied
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetSwapDetailFragment : DaggerFragment() {

    companion object {
        const val TIBET_SWAP_OFFER_KEY = "TIBET_SWAP_OFFER_KEY"
    }

    private lateinit var binding: FragmentTibetSwapDetailBinding
    private lateinit var tibetSwap: TibetSwapExchange


    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val vm: TibetSwapDetailViewModel by viewModels { viewModelFactory }

    private var offerId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offerId = arguments!!.getString(TIBET_SWAP_OFFER_KEY, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTibetSwapDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VLog.d("On Tibet Swap Detail ")
        initDetailTibetSwap()
    }

    private fun initDetailTibetSwap() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.getTibetSwapExchangeOfferId(offerId).collectLatest {
                    binding.initTibetSwapDetail(it)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentTibetSwapDetailBinding.initTibetSwapDetail(item: TibetSwapExchange) {
        edtData.text = formattedTimeForOrderItem(item.time_created)
        val status = item.status
        txtStatus.text = "${getMainActivity().getStringResource(R.string.status_title)}: ${
            getRequestStatusTranslation(
                getMainActivity(),
                status
            )
        }"
        edtSendAmount.text =
            formattedDoubleAmountWithPrecision(item.send_amount) + " ${item.send_coin}"
        edtReceiveAmount.text =
            formattedDoubleAmountWithPrecision(item.receive_amount) + " ${item.receive_coin}"
        edtHashTransaction.text = formatString(7, item.offer_id, 4)
        edtCommissionNetwork.text = formattedDoubleAmountWithPrecision(item.fee)
        if (item.height != 0) {
            edtBlockHeight.text = item.height.toString()
        }
        imgCpyHashTransaction.setOnClickListener {
            makeViewVisibleAndGone(relCopied)
            requireActivity().copyToClipBoard(item.offer_id)
        }
        changeColorTxtStatusRequest(txtStatus, getRequestStatusColor(status, getMainActivity()))

        backLayout.setOnClickListener {
            getMainActivity().popBackStackOnce()
        }

    }

    private fun changeColorTxtStatusRequest(txt: TextView, clr: Int) {
        val text = txt.text.toString()
        val pivot = text.indexOf(":")
        val spannableString = SpannableString(text)
        val textPart =
            ForegroundColorSpan(getMainActivity().getColorResource(R.color.txt_status_request))
        val amountPart = ForegroundColorSpan(clr)
        spannableString.setSpan(textPart, 0, pivot, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spannableString.setSpan(
            amountPart,
            pivot + 1,
            text.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        txt.text = spannableString
    }

}
