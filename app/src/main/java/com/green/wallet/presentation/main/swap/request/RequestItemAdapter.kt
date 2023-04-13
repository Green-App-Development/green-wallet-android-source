package com.green.wallet.presentation.main.swap.request

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.common.tools.requestDateFormat
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.RequestItem
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.RequestStatus
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getDrawableResource

class RequestItemAdapter(
	val activity: MainActivity,
	val listener: OnClickRequestItemListener
) :
	RecyclerView.Adapter<RequestItemAdapter.RequestItemViewHolder>() {

	val data = mutableListOf<RequestItem>()

	fun updateRequestList(incoming: List<RequestItem>) {
		data.clear()
		data.addAll(incoming)
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestItemViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
		return RequestItemViewHolder(view)
	}

	override fun onBindViewHolder(holder: RequestItemViewHolder, position: Int) {
		holder.onBind(data[position])
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

		fun onBind(item: RequestItem) {
			hashId.text = "Заявка #${item.id}"
			when (item.status) {
				RequestStatus.Waiting -> {
					txtSend.text = "Нужно отправить: -${item.send} USDT"
					txtReceive.text = "Вы получите: +${item.receive} XCH"
					dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_blue))
					txtStatus.text = "ожидает оплаты"
				}
				RequestStatus.Cancelled -> {
					txtSend.text = "Отправлено: -"
					txtReceive.text = "Получено: -"
					dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_red))
					txtStatus.text = "отменена"
				}
				RequestStatus.Completed -> {
					txtSend.text = "Отправлено: -${item.send} XCH"
					txtReceive.text = "Получено: +${item.receive} USDT"
					dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_green))
					txtStatus.text = "выполнена"
				}
				RequestStatus.InProgress -> {
					txtSend.text = "Вы отправили: -${item.send} USDT"
					txtReceive.text = "Вы получите: +${item.receive} XCH"
					dotStatus.setImageDrawable(activity.getDrawableResource(R.drawable.ic_dot_orange))
					txtStatus.text = "в процессе"
				}
			}
			changeAmountColor(txtSend, activity.getColorResource(R.color.red_mnemonic))
			changeAmountColor(
				txtReceive,
				activity.getColorResource(if (item.status != RequestStatus.Cancelled) R.color.green else R.color.red_mnemonic)
			)
			txtDate.text = requestDateFormat(item.time_created)
			initChangeColorStatusTxt(item.status, txtStatus)
			detailBtn.setOnClickListener {
				listener.onClickDetailItem(item)
			}
		}

		private fun initChangeColorStatusTxt(status: RequestStatus, txtStatus: TextView) {
			val clr = activity.getColorResource(
				when (status) {
					RequestStatus.Waiting -> R.color.blue_aspect_ratio
					RequestStatus.Cancelled -> R.color.red_mnemonic
					RequestStatus.InProgress -> R.color.orange
					else -> R.color.green
				}
			)
			txtStatus.setTextColor(clr)
		}

		fun changeAmountColor(txt: TextView, clr: Int) {
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

	interface OnClickRequestItemListener {
		fun onClickDetailItem(item: RequestItem)
	}


}
