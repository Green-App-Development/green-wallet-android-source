package com.green.wallet.presentation.main.transaction

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getStringResource

class TransPagingAdapter(
    private val curActivity: MainActivity,
    private val transactionItemListener: TransactionItemAdapter.TransactionListener,
    private val animManager: AnimationManager
) : PagingDataAdapter<Transaction, TransPagingAdapter.TransactionViewHolder>(TransDiffCallback()) {

    private val viewBinderHelper = ViewBinderHelper()

    class TransDiffCallback : DiffUtil.ItemCallback<Transaction>() {

        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.transactionId == newItem.transactionId
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }


    inner class TransactionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val rootLayout: SwipeRevealLayout = v.findViewById(R.id.root_transaction_item)
        private val txtStatus: TextView = v.findViewById(R.id.txtStatus)
        private val txtHeightTransaction: TextView = v.findViewById(R.id.txtHeightTrans)
        private val txtToken: TextView = v.findViewById(R.id.txtToken)
        private val centerLayout: ConstraintLayout = v.findViewById(R.id.centerLayout)
        private val imgSpeedUp: ImageView = v.findViewById(R.id.img_speed_up)
        private val deleteContainer: RelativeLayout = v.findViewById(R.id.container_delete)
        private val imgDelete: ImageView = v.findViewById(R.id.ic_delete)
        private val rootRelative: RelativeLayout = v.findViewById(R.id.root_relative)


        @SuppressLint("SetTextI18n")
        fun onBindTransaction(transaction: Transaction) {
            rootLayout.background.setTint(curActivity.getColorResource(R.color.cardView_background))
            txtStatus.visibility = View.VISIBLE
            txtHeightTransaction.visibility = View.VISIBLE
            txtToken.visibility = View.VISIBLE
            when (transaction.status) {
                Status.Incoming -> {
                    txtStatus.text = curActivity.getStringResource(R.string.transactions_incoming)
                    txtStatus.setTextColor(curActivity.getColorResource(R.color.txt_incoming))
                }

                Status.Outgoing -> {
                    txtStatus.text = curActivity.getStringResource(R.string.incoming_outgoing)
                    txtStatus.setTextColor(curActivity.getColorResource(R.color.txt_outcoming))
                }

                Status.InProgress -> {
                    txtStatus.text = curActivity.getStringResource(R.string.transactions_pendind)
                    txtStatus.setTextColor(curActivity.getColorResource(R.color.txt_inprogress_blue))
                }

                else -> Unit
            }
            txtHeightTransaction.text = "${transaction.confirmedAtHeight}"
            imgSpeedUp.visibility = View.GONE
            txtHeightTransaction.setTextColor(curActivity.getColorResource(R.color.txt_trans_property))
            txtToken.text = "${formattedDoubleAmountWithPrecision(transaction.amount)} ${
                transaction.code
            }"

//            txtToken.text =
//                "0.00000000000 ${
//                    getShortNetworkType(
//                        transaction.networkType
//                    )
//                }"

            rootRelative.setOnClickListener {
                transactionItemListener.onTransactionItemClicked(transaction = transaction)
            }

            imgDelete.setOnClickListener {
                VLog.d("Delete container transaction clicked")
                transactionItemListener.onTransactionDelete(transaction = transaction)
            }

            if (transaction.confirmedAtHeight == 0L) {
                // from the above string
                val mSpannableString = SpannableString("Speed up")

                // Setting underline style from
                // position 0 till length of
                // the spannable string
                mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
                // Displaying this spannable
                // string in TextView
                txtHeightTransaction.text = mSpannableString
                imgSpeedUp.visibility = View.VISIBLE
                txtHeightTransaction.setTextColor(curActivity.getColorResource(R.color.txt_inprogress_blue))
            } else {

            }

            centerLayout.setOnClickListener {
                it.startAnimation(animManager.getBtnEffectAnimation())
                if (transaction.confirmedAtHeight == 0L) {
                    transactionItemListener.onTransactionSpeedUpClick(transaction = transaction)
                } else {
                    transactionItemListener.onTransactionItemClicked(transaction = transaction)
                }
            }

        }

        fun onBindEmptyPlaceHolder() {
            txtStatus.visibility = View.GONE
            txtHeightTransaction.visibility = View.GONE
            txtToken.visibility = View.GONE
        }

    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.onBindTransaction(item)
            viewBinderHelper.setOpenOnlyOne(true)
            viewBinderHelper.bind(holder.rootLayout, "$position")
        } else holder.onBindEmptyPlaceHolder()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

}
