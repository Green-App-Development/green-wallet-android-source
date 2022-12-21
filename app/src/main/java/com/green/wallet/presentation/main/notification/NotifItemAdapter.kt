package com.green.wallet.presentation.main.notification

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.green.wallet.domain.domainmodel.notification.NotificationItem
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.custom.trimNetwork
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getDrawableResource
import com.green.wallet.presentation.tools.getStringResource
import com.example.common.tools.*

class NotifItemAdapter(
	private val activity: Activity,
	private val notifItemListener: NotifItemListener
) :
	RecyclerView.Adapter<NotifItemAdapter.NotifItemViewHolder>() {

	private val notifItemList = mutableListOf<NotificationItem>()

	fun updateNotifItemList(list: List<NotificationItem>) {
		notifItemList.clear()
		notifItemList.addAll(list)
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifItemViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notif, parent, false)
		return NotifItemViewHolder(view)
	}

	override fun onBindViewHolder(holder: NotifItemViewHolder, position: Int) {
		holder.onBind(notifItemList[position])
	}

	override fun getItemCount() = notifItemList.size

	override fun getItemViewType(position: Int): Int {
		return super.getItemViewType(position)
	}

	inner class NotifItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

		private val txtDescription = v.findViewById<TextView>(R.id.notif_description)
		private val txtAmount = v.findViewById<TextView>(R.id.txtAmount)
		private val txtAmountInUSD = v.findViewById<TextView>(R.id.txtAmountInUsd)
		private val txtTime = v.findViewById<TextView>(R.id.txtTime)
		private val rootNotifItem = v.findViewById<RelativeLayout>(R.id.rootNotifItem)
		private val imgNetwork = v.findViewById<ImageView>(R.id.img_coin)
		private val rel_notif_trans = v.findViewById<RelativeLayout>(R.id.rel_notif_trans_detail)
		private val txt_notif_other = v.findViewById<TextView>(R.id.txt_other_notifs)

		@SuppressLint("SetTextI18n")
		fun onBind(notificationItem: NotificationItem) {
			VLog.d(
				"Hour:Minute : ${formattedHourMinute(notificationItem.created_at_time)} and NetworkType : ${notificationItem.networkType}, String res : ${
					activity.getStringResource(
						R.string.notifications_withdrawn_from_wallet
					)
				}"
			)
			txtTime.text = formattedHourMinute(notificationItem.created_at_time)
			if (notificationItem.message.isEmpty()) {
				rel_notif_trans.visibility = View.VISIBLE
				txt_notif_other.visibility = View.GONE
				if (notificationItem.status == Status.Outgoing) {
					txtDescription.text =
						trimNetwork(
							activity.getStringResource(R.string.notifications_withdrawn_from_wallet)
								.replace("%network%", notificationItem.networkType)
						)
					txtAmount.apply {
						setTextColor(activity.getColorResource(R.color.red_mnemonic))
						text =
							"-${formattedDoubleAmountWithPrecision(notificationItem.amount)} ${
								notificationItem.code
							}"
					}
				} else if (notificationItem.status == Status.Incoming) {
					txtDescription.text =
						trimNetwork(
							activity.getStringResource(R.string.notifications_credited_to_wallet)
								.replace("%network%", notificationItem.networkType)
						)
					txtAmount.text =
						"+${formattedDoubleAmountWithPrecision(notificationItem.amount)} ${
							notificationItem.code
						}"
				}
				rootNotifItem.setOnClickListener {
					notifItemListener.onNotificationClicked(notificationItem)
				}
				txtAmountInUSD.text =
					"${formattedDollarWithPrecision(notificationItem.amountInUSD)}$"

				imgNetwork.setImageDrawable(
					if (notificationItem.networkType.lowercase()
							.contains("chia")
					) activity.getDrawableResource(R.drawable.ic_chia)
					else
						activity.getDrawableResource(R.drawable.ic_chives)
				)
			} else {
				rel_notif_trans.visibility = View.GONE
				txt_notif_other.visibility = View.VISIBLE
				txt_notif_other.text = notificationItem.message
			}

		}

	}


	interface NotifItemListener {
		fun onNotificationClicked(notificationItem: NotificationItem)
	}


}
