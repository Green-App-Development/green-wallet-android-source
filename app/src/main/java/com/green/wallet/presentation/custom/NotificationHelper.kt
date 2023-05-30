package com.green.wallet.presentation.custom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import com.green.wallet.R
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.intro.IntroActivity
import com.green.wallet.presentation.intro.IntroActivity.Companion.INTRO_BUNDLE_KEY
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


@AppScope
class NotificationHelper @Inject constructor(
	private val applicationContext: Context,
	private val prefs: PrefsManager
) {

	companion object {
		const val GreenAppChannel = "Green Wallet Messages"
		const val TransactionMessages = "Transactions"
	}

	private val mutex = Mutex()


	suspend fun callGreenAppNotificationMessages(
		message: String,
		create_at_time: Long,
		key: String = "",
		value: String = ""
	) {
		mutex.withLock {
			buildingNotificationChannels()
			val title = "Green Wallet"
			val isNotificationOn =
				prefs.getSettingBoolean(PrefsManager.PUSH_NOTIF_IS_ON, default = true)

			val unique_notif_id = prefs.getSettingInt(PrefsManager.NOTIFICATION_ID, 0)

			VLog.d("Notification got called with message : $message and time : $create_at_time, isNotificationOn : $isNotificationOn")
			if (!isNotificationOn) {
				return
			}

			val bundle = bundleOf()
			bundle.putString(key, value)

			val notifIntent = Intent(applicationContext, IntroActivity::class.java).apply {
				flags = Intent.FLAG_ACTIVITY_NEW_TASK
				action = Intent.ACTION_MAIN
				addCategory(Intent.CATEGORY_LAUNCHER)
				putExtra(INTRO_BUNDLE_KEY, bundle)
				putExtra("code2", "code2")
			}

			val pendingIntent = PendingIntent.getActivity(
				applicationContext,
				unique_notif_id,
				notifIntent,
				FLAG_IMMUTABLE
			)

			val builder = NotificationCompat.Builder(applicationContext, GreenAppChannel)
				.setSmallIcon(R.drawable.ic_chia_white)
				.setContentTitle(title)
				.setContentText(message)
				.setAutoCancel(true)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setContentIntent(pendingIntent)

			val notificationManager = NotificationManagerCompat.from(applicationContext)
			notificationManager.notify(unique_notif_id, builder.build())
			prefs.saveSettingInt(PrefsManager.NOTIFICATION_ID, unique_notif_id + 1)
		}
	}


	fun buildingNotificationChannels() {

		val channels = listOf(GreenAppChannel, TransactionMessages)
		channels.forEach {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
				val channel = NotificationChannel(
					it,
					it,
					importance
				)
				channel.lightColor = Color.MAGENTA
				val notifManager = applicationContext.applicationContext
					.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
				notifManager.createNotificationChannel(channel)
			}
		}
	}

}
