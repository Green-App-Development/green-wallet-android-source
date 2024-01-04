package com.green.wallet.presentation.main.transaction

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getStringResource

class TransPagingAdapter(
	private val curActivity: MainActivity,
	private val transactionItemListener: TransactionItemAdapter.TransactionListener
) : PagingDataAdapter<Transaction, TransPagingAdapter.TransactionViewHolder>(TransDiffCallback()) {


	class TransDiffCallback : DiffUtil.ItemCallback<Transaction>() {

		override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
			return oldItem.transactionId == newItem.transactionId
		}

		override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
			return oldItem == newItem
		}
	}


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

	override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
		val item = getItem(position)
		if (item != null) {
			holder.onBindTransaction(item)
		} else
			holder.onBindEmptyPlaceHolder()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
		val itemView =
			LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
		return TransactionViewHolder(itemView)
	}

}
