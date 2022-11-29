package com.android.greenapp.presentation.main.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.App
import com.android.greenapp.presentation.custom.workmanager.WorkManagerSyncTransactions
import com.android.greenapp.presentation.tools.SYNC_WORK_TAG
import com.example.common.tools.VLog
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppRemovedRecentTaskService : Service() {


	private var recentJob: Job? = null

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_STICKY
	}

	override fun onBind(p0: Intent?): IBinder? {
		return null
	}


	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		VLog.d("On Task Removed for Application")
		(application as App).applicationIsAlive = false
		initWorkManager()
	}


	override fun onDestroy() {
		super.onDestroy()
		recentJob?.cancel()
	}



	private fun initWorkManager() {
		val constraints =
			Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
		val periodicWorkRequest =
			PeriodicWorkRequestBuilder<WorkManagerSyncTransactions>(15000, TimeUnit.MILLISECONDS)
				.setConstraints(constraints)
		WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
			SYNC_WORK_TAG,
			ExistingPeriodicWorkPolicy.REPLACE,
			periodicWorkRequest.build()
		)
	}

}
