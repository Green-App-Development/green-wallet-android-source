package com.android.greenapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.greenapp.data.local.entity.*

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


@Database(
    entities = [AddressEntity::class, WalletEntity::class, TransactionEntity::class, NotifOtherEntity::class, TokenEntity::class],
    version = 18,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val addressDao: AddressDao
    abstract val walletDao: WalletDao
    abstract val transactionDao: TransactionDao
    abstract val notifOtherDao: NotifOtherDao
    abstract val tokenDao:TokenDao

    companion object {
        const val APP_DB_NAME = "green_app_database_name"
    }


}
