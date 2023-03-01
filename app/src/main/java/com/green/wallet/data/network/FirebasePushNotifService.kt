package com.green.wallet.data.network

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirebasePushNotifService : FirebaseMessagingService() {

	@Inject
	lateinit var notifHelper: NotificationHelper

	override fun onCreate() {
		super.onCreate()
		(applicationContext as App).appComponent.fcmComponentBuilder().build().inject(this)
	}

	override fun onNewToken(token: String) {
		super.onNewToken(token)
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		CoroutineScope(Dispatchers.IO).launch {
			notifHelper.callGreenAppNotificationMessages(
				"Message FCM Checking",
				System.currentTimeMillis()
			)
		}
	}

}
