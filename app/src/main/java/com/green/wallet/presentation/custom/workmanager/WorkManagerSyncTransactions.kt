package com.green.wallet.presentation.custom.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject
import javax.inject.Provider

class WorkManagerSyncTransactions(
	context: Context,
	params: WorkerParameters,
	private val blockChainInteract: BlockChainInteract,
	private val greenAppInteract: GreenAppInteract,
) :
	CoroutineWorker(context, params) {


	class Factory @Inject constructor(
		private val blockChainInteract: Provider<BlockChainInteract>,
		private val greenAppInteract: Provider<GreenAppInteract>
	) : ChildWorkerFactory {

		override fun create(appContext: Context, params: WorkerParameters): ListenableWorker {
			return WorkManagerSyncTransactions(
				appContext,
				params,
				blockChainInteract.get(),
				greenAppInteract.get()
			)
		}
	}


	override suspend fun doWork(): Result {
		try {
			checkingPushNotification()
		} catch (ex: Exception) {
			VLog.d("Exception occurred in  work manager with message ${ex.message}")
			return Result.retry()
		}
		return Result.success()
	}

	private suspend fun checkingPushNotification() {
		blockChainInteract.updateBalanceAndTransactionsPeriodically()
		greenAppInteract.requestOtherNotifItems()
	}


}
