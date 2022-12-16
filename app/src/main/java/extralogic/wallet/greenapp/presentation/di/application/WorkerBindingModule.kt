package extralogic.wallet.greenapp.presentation.di.application

import androidx.work.WorkerFactory
import extralogic.wallet.greenapp.presentation.custom.workmanager.ChildWorkerFactory
import extralogic.wallet.greenapp.presentation.custom.workmanager.WorkManagerSyncTransactions
import extralogic.wallet.greenapp.presentation.di.factory.DaggerWorkerFactory
import extralogic.wallet.greenapp.presentation.di.factory.WorkerKey
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