package com.android.greenapp.data.interact

import com.android.greenapp.data.local.NotifOtherDao
import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.domain.domainmodel.notification.NotifSection
import com.android.greenapp.domain.domainmodel.notification.NotificationItem
import com.android.greenapp.domain.interact.NotifInteract
import com.android.greenapp.presentation.tools.Status
import com.example.common.tools.formattedDay
import java.util.*
import javax.inject.Inject

/**
 * Created by bekjan on 27.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class NotifinteractImpl @Inject constructor(
	private val transactionDao: TransactionDao,
	private val notifOtherDao: NotifOtherDao,
	private val tokenDAO: TokenDao
) :
	NotifInteract {

	override suspend fun getReadyNotifSectionWithNotifItems(
		amount: Double?,
		status: Status?,
		at_least_created_time: Long?,
		yesterday: Long?,
		today: Long?
	): List<NotifSection> {

		val transactionsList = transactionDao.getALlTransactionsByGivenParameters(
			null,
			amount,
			null,
			status,
			at_least_created_time,
			yesterday,
			today
		).filter { it.status != Status.InProgress }

		val notifItems = transactionsList.map { trans ->

			val amountInUSD = tokenDAO.getTokenByCode(trans.code).get().price * trans.amount

			NotificationItem(
				trans.status,
				trans.amount,
				amountInUSD,
				trans.networkType,
				trans.height,
				trans.fee_amount,
				trans.created_at_time,
				"",
				trans.code
			)
		}.toMutableList()

		if (status == Status.OTHER)
			notifOtherDao.getALlNotifOthersByGivenParameters(
				at_least_created_time,
				yesterday,
				today
			).forEach {
				notifItems.add(
					NotificationItem(
						created_at_time = it.created_at_time,
						message = it.message
					)
				)
			}

		return getReadyRecViewNotifItemsDividedSections(notifItems)
	}


	private fun getReadyRecViewNotifItemsDividedSections(notificationList: List<NotificationItem>): List<NotifSection> {
		val daysWithNotifications = hashMapOf<String, MutableList<NotificationItem>>()
		for (notif in notificationList) {
			val datePattern = formattedDay(notif.created_at_time)
			if (daysWithNotifications[datePattern] == null)
				daysWithNotifications[datePattern] = mutableListOf()
			daysWithNotifications[datePattern]!!.add(notif)
		}
		val readyRecViewItems = mutableListOf<NotifSection>()
		for ((key, value) in daysWithNotifications) {
			readyRecViewItems.add(NotifSection(key, value[0].created_at_time, value))
		}
		return readyRecViewItems.sortedWith(
			object : Comparator<NotifSection> {
				override fun compare(p0: NotifSection, p1: NotifSection): Int {
					return if (p1.actualTime > p0.actualTime)
						1
					else
						-1
				}
			})
	}


}
