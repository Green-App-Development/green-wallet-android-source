package com.android.greenapp.presentation.di.application

import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.android.greenapp.presentation.custom.workmanager.ChildWorkerFactory
import com.android.greenapp.presentation.custom.workmanager.WorkManagerSyncTransactions
import com.android.greenapp.presentation.di.factory.DaggerWorkerFactory
import com.android.greenapp.presentation.di.factory.WorkerKey
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