package com.green.wallet.presentation.di.application

import androidx.work.WorkerFactory
import com.green.wallet.presentation.custom.workmanager.ChildWorkerFactory
import com.green.wallet.presentation.custom.workmanager.WorkManagerSyncTransactions
import com.green.wallet.presentation.di.factory.DaggerWorkerFactory
import com.green.wallet.presentation.di.factory.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class WorkerBindingModule {

    @Binds
    @IntoMap
    @WorkerKey(WorkManagerSyncTransactions::class)
    abstract fun bindPrepopulateCategoryWork(factory: WorkManagerSyncTransactions.Factory): ChildWorkerFactory

    @Binds
    abstract fun bindWorkManagerFactory(factory: DaggerWorkerFactory): WorkerFactory

}
