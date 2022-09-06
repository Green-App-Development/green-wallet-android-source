package com.android.greenapp.presentation.main.transaction

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.formattedDoubleAmountWithPrecision
import com.android.greenapp.presentation.custom.getShortNetworkType
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.Status
import com.android.greenapp.presentation.tools.getColorResource
import com.android.greenapp.presentation.tools.getStringResource

/**
 * Created by bekjan on 12.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class TransactionItemAdapter(
    private val effect: AnimationManager,
    private val curActivity: MainActivity,
    private val transactionItemListener: TransactionListener
) :
    RecyclerView.Adapter<TransactionItemAdapter.TransactionViewHolder>() {

    var itemCountFitsScreen = 0
    private var transactionList = mutableListOf<Transaction>()
    var curSortedStatus: Status? = null

    fun updateTransactionList(transactions: List<Transaction>) {
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
        } else
            holder.onBindEmptyPlaceHolder()
    }

    override fun getItemCount() = Math.max(transactionList.size, itemCountFitsScreen)


    inner class TransactionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val rootLayout: RelativeLayout = v.findViewById(R.id.root_transaction_item)
        private val txtStatus: TextView = v.findViewById(R.id.txtStatus)
        private val txtHeightTransaction: TextView = v.findViewById(R.id.txtHeightTrans)
        private val txtToken: TextView = v.findViewById(R.id.txtToken)

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
                    txtStatus.setTextColor(curActivity.getColorResource(R.color.txt_inprogress))
                }
            }
            txtHeightTransaction.text = "${transaction.confirmed_at_height}"
            txtToken.text =
                "${formattedDoubleAmountWithPrecision(transaction.amount)} ${
                    getShortNetworkType(
                        transaction.networkType
                    )
                }"

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
        fun onTransactionItemClicked(transaction: Transaction)
    }

}