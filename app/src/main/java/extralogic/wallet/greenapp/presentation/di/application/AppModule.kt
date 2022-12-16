package extralogic.wallet.greenapp.presentation.di.application

import android.content.Context
import androidx.room.Room
import extralogic.wallet.greenapp.data.local.AppDatabase
import com.example.common.tools.VLog
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineExceptionHandler


@Module
class AppModule {


	@Provides
	fun provideGson() = Gson()

	@AppScope
	@Provides
	fun provideAppDatabase(context: Context): AppDatabase {
		return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.APP_DB_NAME)
			.fallbackToDestructiveMigration().build()
	}

	@Provides
	fun provideAddressDao(appDatabase: AppDatabase) = appDatabase.addressDao

	@Provides
	fun provideWalletDao(appDatabase: AppDatabase) = appDatabase.walletDao

	@Provides
	fun provideTransactionDao(appDatabase: AppDatabase) = appDatabase.transactionDao

	@Provides
	fun provideNotifOtherDao(appDatabase: AppDatabase) = appDatabase.notifOtherDao

	@Provides
	fun provideTokenDao(appDatabase: AppDatabase) = appDatabase.tokenDao

	@Provides
	fun provideSpentCoinsDao(appDatabase: AppDatabase) = appDatabase.spentCoinsDao

	@Provides
	fun provideFAQDao(appDatabase: AppDatabase) = appDatabase.faqDao

	@Provides
	fun provideHandler(): CoroutineExceptionHandler {
		return CoroutineExceptionHandler { _, t ->
			VLog.d("Caught Exception in coroutine : ${t.message}")
		}
	}


}
