package com.green.wallet.presentation.main.swap.qrsend

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.green.wallet.R
import com.green.wallet.databinding.FragmentQrcodeSendBinding
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.tools.FIFTEEN_MINUTES_IN_MILLIS_SECONDS
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentQRSend : Fragment() {

    private lateinit var binding: FragmentQrcodeSendBinding


    companion object {
        const val SEND_ADDRESS_KEY = "send_address_key"
        const val ORDER_ITEM_KEY = "order_item_key"
    }

    private var address = ""
    private lateinit var orderItem: OrderItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            address = it.getString(SEND_ADDRESS_KEY, "")
            val arg = it.get(ORDER_ITEM_KEY)
            if (arg is OrderItem) {
                orderItem = arg
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrcodeSendBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentQrcodeSendBinding.initViews() {
        try {
            val qrgEncoder =
                QRGEncoder(orderItem.giveAddress, null, QRGContents.Type.TEXT, 1500)
            qrgEncoder.colorBlack =
                ContextCompat.getColor(getMainActivity(), R.color.qr_black)
            qrgEncoder.colorWhite =
                ContextCompat.getColor(getMainActivity(), R.color.qr_white)
            val bitMap = qrgEncoder.bitmap
            qrImg.setImageBitmap(bitMap)
        } catch (ex: Exception) {
            VLog.e("Exception in creating bitmap : ${ex.message}")
        }

        backLayout.setOnClickListener {
            getMainActivity().popBackStackOnce()
        }

        txtAddressWallet.text = orderItem.giveAddress
        edtNeedToSendAmount.text =
            "${formattedDoubleAmountWithPrecision(orderItem.amountToSend)} ${orderItem.sendCoin}"
        edtReceiveAmount.text =
            "${formattedDoubleAmountWithPrecision(orderItem.amountToReceive)} ${orderItem.getCoin}"

        relCopy.setOnClickListener {
            changeVisibleRelCopiedView()
        }

        imgCpySendAmount.setOnClickListener {
            changeVisibleRelCopiedView()
        }

        btnShare.setOnClickListener {
            binding.btnShare.setOnClickListener {
                getMainActivity().launchingIntentForSendingWalletAddress(binding.txtAddressWallet.text.toString())
            }
        }

        val timeDiff =
            ((orderItem.timeCreated + FIFTEEN_MINUTES_IN_MILLIS_SECONDS) - System.currentTimeMillis()) / 1000

        startTimerAwaitingPayment(edtAutoCancelTime, timeDiff)

    }

    private var job: Job? = null

    fun changeVisibleRelCopiedView() {
        binding.relCopied.visibility = View.VISIBLE
        job?.cancel()
        job = lifecycleScope.launch {
            delay(2000L)
            binding.relCopied.visibility = View.GONE
        }
    }

    fun startTimerAwaitingPayment(txt: TextView, diff: Long) {
        VLog.d("Method time called : $diff")
        lifecycleScope.launch {
            var totalSeconds = diff
            VLog.d("Method time called after scope launched")
            while (totalSeconds >= 0) {
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                VLog.d("Method time called timer : ${totalSeconds}")
                txt.text = "${addZeroToFrontIfNeeded(minutes)}:${addZeroToFrontIfNeeded(seconds)}"
                totalSeconds--
                delay(1000)
            }
            txt.text="00:00"
        }
    }

    fun addZeroToFrontIfNeeded(num: Long): String {
        val str = "$num"
        if (str.length == 2)
            return str
        return "0$str"
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }


}


