package com.green.wallet.presentation.main.transaction

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getStringResource


class TransactionItemAdapter(
    private val effect: AnimationManager,
    private val curActivity: MainActivity,
    private val transactionItemListener: TransactionListener
) :
    RecyclerView.Adapter<TransactionItemAdapter.TransactionViewHolder>() {

    var itemCountFitsScreen = 0
    private var transactionList = mutableListOf<TransferTransaction>()
    var curSortedStatus: Status? = null
    private val viewBinderHelper = ViewBinderHelper()

    fun updateTransactionList(transactions: List<TransferTransaction>) {
        transactionList.clear()
        transactionList.addAll(transactions)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        if (position < transactionList.size) {
            holder.onBindTransaction(transactionList[position])
            viewBinderHelper.setOpenOnlyOne(true)
            viewBinderHelper.bind(holder.rootLayout, "$position")
        } else
            holder.onBindEmptyPlaceHolder()
    }

    override fun getItemCount() = Math.max(transactionList.size, itemCountFitsScreen)


    inner class TransactionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val rootLayout: SwipeRevealLayout = v.findViewById(R.id.root_transaction_item)
        private val txtStatus: TextView = v.findViewById(R.id.txtStatus)
        private val txtHeightTransaction: TextView = v.findViewById(R.id.txtHeightTrans)
        private val txtToken: TextView = v.findViewById(R.id.txtToken)

        @SuppressLint("SetTextI18n")
        fun onBindTransaction(transaction: TransferTransaction) {
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
                    txtStatus.setTextColor(curActivity.getColorResource(R.color.txt_inprogress))
                }

                else -> Unit
            }
            txtHeightTransaction.text = "${transaction.confirmedAtHeight}"
            txtToken.text =
                "${formattedDoubleAmountWithPrecision(transaction.amount)} ${
                    transaction.code
                }"

//            txtToken.text =
//                "0.00000000000 ${
//                    getShortNetworkType(
//                        transaction.networkType
//                    )
//                }"

            rootLayout.setOnClickListener {
                transactionItemListener.onTransactionItemClicked(transaction = transaction)
            }

        }

        fun onBindEmptyPlaceHolder() {
            txtStatus.visibility = View.GONE
            txtHeightTransaction.visibility = View.GONE
            txtToken.visibility = View.GONE
        }

    }

    interface TransactionListener {
        fun onTransactionItemClicked(transaction: TransferTransaction)

        fun onTransactionSpeedUpClick(transaction: TransferTransaction)

        fun onTransactionDelete(transaction: TransferTransaction)

    }

}
