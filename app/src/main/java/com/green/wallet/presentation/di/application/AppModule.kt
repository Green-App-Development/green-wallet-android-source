package com.green.wallet.presentation.di.application

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.green.wallet.data.local.AppDatabase
import com.green.wallet.data.local.AppDatabase.Companion.migration25To34
import com.green.wallet.data.local.AppDatabase.Companion.migration34To35
import com.green.wallet.data.local.AppDatabase.Companion.migration35To36
import com.green.wallet.data.local.AppDatabase.Companion.migration37To38
import com.green.wallet.data.local.AppDatabase.Companion.migration6To37
import com.green.wallet.presentation.custom.encryptor.EncryptorProvider
import com.green.wallet.presentation.custom.encryptor.EncryptorProviderImpl
import com.green.wallet.presentation.custom.encryptor.EncryptorProviderImpl.Companion.SECOND_STAGE
import com.green.wallet.presentation.tools.VLog
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
            .addMigrations(
                migration25To34, migration34To35, migration35To36, migration6To37,
                migration37To38
            )
            .build()
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
    fun provideNFtCoinsDao(appDatabase: AppDatabase) = appDatabase.nftCoinsDao

    @Provides
    fun provideNFtInfoDao(appDatabase: AppDatabase) = appDatabase.nftInfoDao

    @Provides
    fun provideOrderExchangeDao(appDatabase: AppDatabase) = appDatabase.orderExchangeDao

    @Provides
    fun provideTibetDao(appDatabase: AppDatabase) = appDatabase.tibetDao

    @Provides
    fun provideHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, t ->
            VLog.d("Caught Exception in coroutine : ${t.message}")
        }
    }


    @Provides
    fun provideEncryptor(context: Context): EncryptorProvider {
        val encryptor = EncryptorProviderImpl(context)
        encryptor.setStage(SECOND_STAGE)
        return encryptor
    }

    @Provides
    fun provideOfferTransactionDao(appDatabase: AppDatabase) = appDatabase.offerTransDao

}
