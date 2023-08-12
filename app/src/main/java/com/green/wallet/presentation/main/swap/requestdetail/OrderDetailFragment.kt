package com.green.wallet.presentation.main.swap.requestdetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.common.tools.formatString
import com.example.common.tools.formattedTimeForOrderItem
import com.example.common.tools.getRequestStatusColor
import com.example.common.tools.getRequestStatusTranslation
import com.green.wallet.R
import com.green.wallet.databinding.FragmentRequestDetailsBinding
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.presentation.custom.ConnectionLiveData
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.*
import com.green.wallet.presentation.viewBinding
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrderDetailFragment : DaggerFragment() {


    companion object {
        const val ORDER_HASH = "order_hash"
    }

    private val binding by viewBinding(FragmentRequestDetailsBinding::bind)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val vm: OrderDetailViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    @Inject
    lateinit var dialogMan: DialogManager


    private var orderHash = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VLog.d("Arguments on request Detail : $arguments")
        arguments?.let {
            orderHash = it.getString(ORDER_HASH, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_request_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerClicks()
        if (orderHash.isNotEmpty()) {
            binding.initUpdateOrderDetails()
        }
    }

    private fun FragmentRequestDetailsBinding.registerClicks() {

        backLayout.setOnClickListener {
            getMainActivity().popBackStackOnce()
        }

    }

    private var orderDetailsJob: Job? = null

    @SuppressLint("SetTextI18n")
    private fun FragmentRequestDetailsBinding.initUpdateOrderDetails() {
        orderDetailsJob?.cancel()
        orderDetailsJob = lifecycleScope.launch {
            val orderItem = vm.getOrderItemByHash(orderHash)
            VLog.d("OrderItem from vm : $orderItem")
            val status = orderItem.status
            txtStatus.text = "${getMainActivity().getStringResource(R.string.status_title)}: ${
                getRequestStatusTranslation(
                    getMainActivity(),
                    status
                )
            }"
            txtRequestHash.text =
                getMainActivity().getStringResource(R.string.order_title) + " #${orderItem.hash}"
            edtData.text = formattedTimeForOrderItem(orderItem.timeCreated)
            if (status == OrderStatus.Cancelled) {
                statusCancelled()
                return@launch
            }
            edtAddressEksCrou.text = formatString(9, orderItem.giveAddress, 6)
            edtSendAmount.text = "${orderItem.amountToSend} " + orderItem.sendCoin
            val amountToReceive = orderItem.amountToReceive
            edtReceiveAmount.text = "$amountToReceive " + orderItem.getCoin
            edtAddressReceive.text = formatString(9, orderItem.getAddress, 6)
            edtCourseExchange.text =
                "1 ${orderItem.sendCoin} = ${
                    formattedDollarWithPrecision(
                        orderItem.rate
                    )
                }"
            if (orderItem.txID.isNotEmpty())
                edtHashTransaction.text = formatString(9, orderItem.txID, 6)
            else {
                imgCpyHashTransaction.visibility = View.GONE
            }
            edtCommissionNetwork.text =
                formattedDoubleAmountWithPrecision(orderItem.commission_fee) + "XCH"
            edtCommissionTron.setText("${orderItem.commission_tron}$")
            edtCommissionExchange.setText("${orderItem.commission_percent}%")
            changeColorTxtStatusRequest(txtStatus, getRequestStatusColor(status, getMainActivity()))
            val params = scrollViewProperties.layoutParams as ConstraintLayout.LayoutParams
            when (status) {
                OrderStatus.Waiting -> {
                    layoutWaiting.visibility = View.VISIBLE
                    params.bottomToTop = R.id.layout_waiting
                    val timeDiff =
                        ((orderItem.timeCreated + FIFTEEN_MINUTES_IN_MILLIS_SECONDS) - System.currentTimeMillis()) / 1000
                    startTimerAwaitingPayment(edtAutoCancelTime, timeDiff)
                }

                else -> {
                    params.bottomToBottom = R.id.root
                }
            }
            scrollViewProperties.layoutParams = params
            txtAutoCancel.text = "${getMainActivity().getStringResource(R.string.auto_cancel)}:"
            if (status == OrderStatus.Waiting)
                txtSent.text = getMainActivity().getStringResource(R.string.need_to_send)
            if (status == OrderStatus.InProgress || status == OrderStatus.Waiting) {
                txtReceive.text = getMainActivity().getStringResource(R.string.you_will_receive)
            }
            txtFinishTran.text =
                getMainActivity().getStringResource(R.string.completion_oper_flow) + ":"

            imgCpyAddress.setOnClickListener {
                getMainActivity().copyToClipBoard(orderItem.giveAddress)
            }

            imgCpyReceiveAddress.setOnClickListener {
                getMainActivity().copyToClipBoard(orderItem.getAddress)
            }

            imgCpyHashTransaction.setOnClickListener {
                getMainActivity().copyToClipBoard(orderItem.txID)
            }

            imgCpyRequestHash.setOnClickListener {
                getMainActivity().copyToClipBoard(orderItem.hash)
            }

            initClickListeners(orderItem)

            refresh.apply {
                setOnRefreshListener {
                    VLog.d("Is Online ${connectionLiveData.isOnline}")
                    if (connectionLiveData.isOnline) {
                        vm.updateOrdersStatus {
                            isRefreshing = false
                        }
                    } else {
                        isRefreshing = false
                        dialogMan.showNoInternetTimeOutExceptionDialog(requireActivity()) {

                        }
                    }
                }
                setColorSchemeResources(R.color.green)
            }

        }
    }

    private fun initClickListeners(orderItem: OrderItem) {
        binding.apply {
            btnPay.setOnClickListener {
                if (orderItem.sendCoin == "XCH") {
                    getMainActivity().move2SwapSendXCHFragment(
                        orderItem.giveAddress,
                        orderItem.amountToSend
                    )
                } else
                    getMainActivity().move2BtmDialogPayment(
                        orderItem.giveAddress,
                        orderItem.amountToSend,
                        orderItem
                    )
            }
        }
    }

    fun startTimerAwaitingPayment(txt: TextView, diff: Long) {
        lifecycleScope.launch {
            var totalSeconds = diff
            while (totalSeconds >= 0) {
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                txt.text = "${addZeroToFrontIfNeeded(minutes)}:${addZeroToFrontIfNeeded(seconds)}"
                totalSeconds--
                delay(1000)
            }
            txt.text = "00:00"
            vm.updateOrdersStatus {

            }
        }
    }

    fun addZeroToFrontIfNeeded(num: Long): String {
        val str = "$num"
        if (str.length == 2)
            return str
        return "0$str"
    }

    private fun statusCancelled() {
        binding.apply {
            listOf(imgCpyAddress, imgCpyReceiveAddress, imgCpyHashTransaction).forEach {
                it.visibility = View.GONE
            }

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
