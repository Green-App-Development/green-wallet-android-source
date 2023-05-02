package com.green.wallet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.green.wallet.data.local.entity.*

@Database(
	entities = [
		AddressEntity::class, WalletEntity::class, TransactionEntity::class,
		NotifOtherEntity::class, TokenEntity::class,
		SpentCoinsEntity::class, FaqItemEntity::class,
		NFTInfoEntity::class, NFTCoinEntity::class
	],
	version = 31,
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
	abstract val nftCoinsDao: NftCoinsDao
	abstract val nftInfoDao:NftInfoDao

	companion object {
		const val APP_DB_NAME = "green_app_database_name"
	}


}
