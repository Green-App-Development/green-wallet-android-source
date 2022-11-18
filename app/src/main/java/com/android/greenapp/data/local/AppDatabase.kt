package com.android.greenapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.greenapp.data.local.entity.*
import com.android.greenapp.domain.domainmodel.FAQItem

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


@Database(
	entities = [AddressEntity::class, WalletEntity::class, TransactionEntity::class, NotifOtherEntity::class, TokenEntity::class, SpentCoinsEntity::class, FaqItemEntity::class],
	version = 23,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

	abstract val addressDao: AddressDao
	abstract val walletDao: WalletDao
	abstract val transactionDao: TransactionDao
	abstract val notifOtherDao: NotifOtherDao
	abstract val tokenDao: TokenDao
	abstract val spentCoinsDao: SpentCoinsDao
	abstract val faqDao: FAQDao

	companion object {
		const val APP_DB_NAME = "green_app_database_name"
	}


}
