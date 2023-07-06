package com.green.wallet.presentation.main.swap.order

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.common.tools.requestDateFormat
import com.green.wallet.R
import com.green.wallet.databinding.ItemRequestTibetLiquidityBinding
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getDrawableResource
import com.green.wallet.presentation.tools.getStringResource
import kotlinx.android.synthetic.main.fragment_exchange.view.container

class OrderItemAdapter(
    val activity: MainActivity,
    val listener: OnClickRequestItemListener
) :
    RecyclerView.Adapter<ViewHolder>() {

    var data = listOf<Any>()

    fun updateRequestList(incoming: List<Any>) {
        val diffResult = DiffUtil.calculateDiff(OrdersUtilCallback(data, incoming))
        data = incoming
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_ORDER -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_request_order, parent, false)

                RequestItemViewHolder(view)
            }

            TYPE_TIBET_LIQUIDITY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_request_tibet_liquidity, parent, false)
                OrderItemTibetLiquidity(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_request_order, parent, false)

                RequestItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is RequestItemViewHolder -> {
                when (data[position]) {
                    is OrderItem ->
                        holder.onBindOrderItem(data[position] as OrderItem)

                    is TibetSwapExchange ->
                        holder.onBindTibetSwapItem(data[position] as TibetSwapExchange)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val element = data[position]
        return when (element) {
            is OrderItem -> TYPE_ORDER
            is TibetLiquidity -> TYPE_TIBET_LIQUIDITY
            else -> TYPE_TIBET_SWAP
        }
    }

    override fun getItemCount() = data.size

    inner class RequestItemViewHolder(v: View) : ViewHolder(v) {

        val hashId = v.findViewById<TextView>(R.id.txtRequestHash)
        val dotStatus = v.findViewById<ImageView>(R.id.img_dot)
        val txtStatus = v.findViewById<TextView>(R.id.txtStatusRequest)
        val txtSend = v.findViewById<TextView>(R.id.txtSendAmount)
        val txtReceive = v.findViewById<TextView>(R.id.txtReceiveAmount)
        val detailBtn = v.findViewById<TextView>(R.id.txtDetailRequest)
        val txtDate = v.findViewById<TextView>(R.id.txtDate)

        fun onBindOrderItem(item: OrderItem) {
            hashId.text = "${activity.getStringResource(R.string.order_title)} #${item.hash}"
            val amountToReceive = formattedDollarWithPrecision(item.amountToSend * item.rate, 4)
            when (item.status) {
                OrderStatus.Waiting -> {
                    txtSend.text =
                        "${activity.getStringResource(R.string.need_to_send)}: -${item.amountToSend} ${item.sendCoin}"
                    txtReceive.text =
                        "${activity.getStringResource(R.string.you_will_receive)}: +${amountToReceive} ${item.getCoin}"
                    dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_blue))
                    txtStatus.text = activity.getStringResource(R.string.awaiting_payment)
                }

                OrderStatus.Cancelled -> {
                    txtSend.text = "${activity.getStringResource(R.string.sent_flow)}: -"
                    txtReceive.text = "${activity.getStringResource(R.string.received_flow)}: -"
                    dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_red))
                    txtStatus.text = activity.getString(R.string.status_canceled)
                }

                OrderStatus.Success -> {
                    txtSend.text =
                        "${activity.getStringResource(R.string.sent_flow)}: -${item.amountToSend} ${item.sendCoin}"
                    txtReceive.text =
                        "${activity.getStringResource(R.string.received_flow)}: +${amountToReceive} ${item.getCoin}"
                    dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_green))
                    txtStatus.text = activity.getStringResource(R.string.status_completed)
                }

                OrderStatus.InProgress -> {
                    txtSend.text =
                        "${activity.getStringResource(R.string.you_sent_flow)}: -${item.amountToSend} ${item.sendCoin}"
                    txtReceive.text =
                        "${activity.getStringResource(R.string.you_will_receive)}: +${amountToReceive} ${item.getCoin}"
                    dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_orange))
                    txtStatus.text = activity.getStringResource(R.string.status_in_process)
                }
            }
            changeAmountColor(txtSend, activity.getColorResource(R.color.red_mnemonic))
            changeAmountColor(
                txtReceive,
                activity.getColorResource(if (item.status != OrderStatus.Cancelled) R.color.green else R.color.red_mnemonic)
            )
            txtDate.text = requestDateFormat(item.timeCreated)
            initChangeColorStatusTxt(item.status, txtStatus)
            detailBtn.setOnClickListener {
                listener.onClickDetailItem(item)
            }
        }

        fun onBindTibetSwapItem(item: TibetSwapExchange) {
            hashId.text = "Create Offer"
            val formatSend = formattedDoubleAmountWithPrecision(item.send_amount)
            val formatReceive = formattedDoubleAmountWithPrecision(item.receive_amount)
            when (item.status) {
                OrderStatus.Success -> {
                    txtSend.text =
                        "${activity.getStringResource(R.string.sent_flow)}: -${formatSend} ${item.send_coin}"
                    txtReceive.text =
                        "${activity.getStringResource(R.string.received_flow)}: +${formatReceive} ${item.receive_coin}"
                    dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_green))
                    txtStatus.text = activity.getStringResource(R.string.status_completed)
                }

                OrderStatus.InProgress -> {
                    txtSend.text =
                        "${activity.getStringResource(R.string.you_sent_flow)}: -${formatSend} ${item.send_coin}"
                    txtReceive.text =
                        "${activity.getStringResource(R.string.you_will_receive)}: +${formatReceive} ${item.receive_coin}"
                    dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_orange))
                    txtStatus.text = activity.getStringResource(R.string.status_in_process)
                }
            }
            changeAmountColor(txtSend, activity.getColorResource(R.color.red_mnemonic))
            changeAmountColor(
                txtReceive,
                activity.getColorResource(if (item.status != OrderStatus.Cancelled) R.color.green else R.color.red_mnemonic)
            )
            txtDate.text = requestDateFormat(item.time_created)
            initChangeColorStatusTxt(item.status, txtStatus)
            detailBtn.setOnClickListener {
                listener.onClickDetailItem(item)
            }
        }

        private fun initChangeColorStatusTxt(status: OrderStatus, txtStatus: TextView) {
            val clr = activity.getColorResource(
                when (status) {
                    OrderStatus.Waiting -> R.color.blue_aspect_ratio
                    OrderStatus.Cancelled -> R.color.red_mnemonic
                    OrderStatus.InProgress -> R.color.orange
                    else -> R.color.green
                }
            )
            txtStatus.setTextColor(clr)
        }

        private fun changeAmountColor(txt: TextView, clr: Int) {
            val text = txt.text.toString()
            val pivot = text.indexOf(":")
            val spannableString = SpannableString(text)
            val textPart =
                ForegroundColorSpan(activity.getColorResource(R.color.sorting_txt_category))
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

    inner class OrderItemTibetLiquidity(val view: View) : ViewHolder(view) {

        private lateinit var binding: ItemRequestTibetLiquidityBinding

        fun onBind(tibetLiquidity: TibetLiquidity) {
            binding = ItemRequestTibetLiquidityBinding.inflate()
        }

    }


    private companion object {
        private const val TYPE_ORDER = 1
        private const val TYPE_TIBET_SWAP = 2
        private const val TYPE_TIBET_LIQUIDITY = 3
    }


    inner class OrdersUtilCallback(
        val oldAddressList: List<Any>,
        val newAddressList: List<Any>
    ) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldAddressList.size
        }

        override fun getNewListSize(): Int {
            return newAddressList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldAddressList[oldItemPosition] == newAddressList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldAddressList[oldItemPosition] == newAddressList[newItemPosition]
        }

    }

    interface OnClickRequestItemListener {
        fun onClickDetailItem(item: Any)
    }


}
