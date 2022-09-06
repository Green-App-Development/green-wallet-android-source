package com.android.greenapp.presentation.main.notification

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.domain.entity.notification.NotifSection
import com.android.greenapp.domain.entity.notification.NotificationItem
import com.android.greenapp.presentation.custom.getTranslatedMonth
import com.example.common.tools.formattedDateMonth
import com.example.common.tools.formattedTodaysDate
import com.example.common.tools.formattedYesterdaysDate
import com.android.greenapp.presentation.tools.getStringResource

class NotifSectionAdapter(
    private val activity: Activity,
    private val parentNotifListener: ParentNotifListener
) :
    RecyclerView.Adapter<NotifSectionAdapter.NotifSectionViewHolder>() {

    private val notifSectionList = mutableListOf<NotifSection>()

    fun updateNotifSectionList(list: List<NotifSection>) {
        notifSectionList.clear()
        notifSectionList.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifSectionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notif_section, parent, false)
        return NotifSectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifSectionViewHolder, position: Int) {
        holder.onBind(notifSectionList[position])
    }

    override fun getItemCount() = notifSectionList.size

    inner class NotifSectionViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val txtDate = v.findViewById<TextView>(R.id.txtDate)
        private val recViewNotifItem = v.findViewById<RecyclerView>(R.id.rec_view_notif_items)

        @SuppressLint("SetTextI18n")
        fun onBind(notifSection: NotifSection) {
            val notifItemAdapter =
                NotifItemAdapter(activity, object : NotifItemAdapter.NotifItemListener {
                    override fun onNotificationClicked(notificationItem: NotificationItem) {
                        parentNotifListener.onNotifClicked(notificationItem = notificationItem)
                    }
                })
            recViewNotifItem.adapter = notifItemAdapter
            notifItemAdapter.updateNotifItemList(notifSection.notifs)

            val dayMonth =
                formattedDateMonth(notifSection.actualTime).split(":").map { Integer.valueOf(it) }
                    .toList()

            val todayDate = Integer.valueOf(formattedTodaysDate())
            val yestedaysDate = Integer.valueOf(formattedYesterdaysDate())
            var todayOrYesterday =
                if (todayDate == dayMonth[0]) activity.getStringResource(R.string.notifications_today) else if (yestedaysDate == dayMonth[0]) activity.getStringResource(
                    R.string.notifications_yesterday
                ) else ""
            if (todayOrYesterday.isNotEmpty())
                todayOrYesterday += ", "
            txtDate.text = "$todayOrYesterday${dayMonth[0]} ${
                getTranslatedMonth(
                    activity,
                    dayMonth[1]
                )
            }"
        }
    }

    interface ParentNotifListener {
        fun onNotifClicked(notificationItem: NotificationItem)
    }


}