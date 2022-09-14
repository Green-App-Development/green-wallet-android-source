package com.android.greenapp.presentation.custom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.greenapp.R
import com.android.greenapp.presentation.di.application.AppScope
import com.android.greenapp.presentation.intro.IntroActivity
import com.example.common.tools.VLog
import javax.inject.Inject


@AppScope
class NotificationHelper @Inject constructor(private val applicationContext: Context) {

    private val greenAppChannel = "Green App Messages"

    fun callGreenAppNotificationMessages(message: String, create_at_time: Long) {
        buildingNotificationChannels()
        VLog.d("Notification got called with message : $message and time : $create_at_time")
        val title = "Green App Message"

        val notifIntent = Intent(applicationContext, IntroActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            notifIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, greenAppChannel)
            .setSmallIcon(R.drawable.ic_chia_white)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(101, builder.build())
    }


    fun buildingNotificationChannels() {

        val channels = listOf(greenAppChannel)
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