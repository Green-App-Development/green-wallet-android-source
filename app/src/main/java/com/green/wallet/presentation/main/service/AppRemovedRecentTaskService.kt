package com.green.wallet.presentation.main.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.workmanager.WorkManagerSyncTransactions
import com.green.wallet.presentation.tools.SYNC_WORK_TAG
import com.example.common.tools.VLog
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit

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
	}


	override fun onDestroy() {
		super.onDestroy()
		recentJob?.cancel()
	}





}
