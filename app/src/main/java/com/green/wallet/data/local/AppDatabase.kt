package com.green.wallet.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.green.wallet.data.local.entity.*

@Database(
	entities = [
		AddressEntity::class, WalletEntity::class, TransactionEntity::class,
		NotifOtherEntity::class, TokenEntity::class,
		SpentCoinsEntity::class, FaqItemEntity::class,
		NFTInfoEntity::class, NFTCoinEntity::class
	],
	version = 34,
	exportSchema = true,
	autoMigrations = [
		AutoMigration(from = 25, to = 34)
	]
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
	abstract val nftInfoDao: NftInfoDao

	companion object {

		const val APP_DB_NAME = "green_app_database_name"

		val migration25To34 = object : Migration(25, 34) {

			override fun migrate(database: SupportSQLiteDatabase) {

				database.execSQL(
					"CREATE TABLE IF NOT EXISTS `NFTInfoEntity` (" +
							"`nft_coin_hash` TEXT NOT NULL, " +
							"`nft_id` TEXT NOT NULL, " +
							"`launcher_id` TEXT NOT NULL, " +
							"`owner_did` TEXT NOT NULL, " +
							"`minter_did` TEXT NOT NULL, " +
							"`royalty_percentage` INTEGER NOT NULL, " +
							"`mint_height` INTEGER NOT NULL, " +
							"`data_url` TEXT NOT NULL, " +
							"`data_hash` TEXT NOT NULL, " +
							"`meta_hash` TEXT NOT NULL, " +
							"`meta_url` TEXT NOT NULL, " +
							"`description` TEXT NOT NULL, " +
							"`collection` TEXT NOT NULL, " +
							"`properties` TEXT NOT NULL, " +
							"`name` TEXT NOT NULL, " +
							"`address_fk` TEXT NOT NULL, " +
							"`spent` INTEGER NOT NULL, " +
							"PRIMARY KEY(`nft_coin_hash`), " +
							"FOREIGN KEY(`address_fk`) REFERENCES `WalletEntity`(`address`) ON DELETE CASCADE)"
				)

				database.execSQL(
					"CREATE TABLE IF NOT EXISTS `NFTCoinEntity` (" +
							"`coin_info` TEXT NOT NULL, " +
							"`address_fk` TEXT NOT NULL, " +
							"`coin_hash` TEXT NOT NULL, " +
							"`amount` INTEGER NOT NULL, " +
							"`confirmed_block_index` INTEGER NOT NULL, " +
							"`spent_block_index` INTEGER NOT NULL, " +
							"`time_stamp` INTEGER NOT NULL, " +
							"`puzzle_hash` TEXT NOT NULL, " +
							"PRIMARY KEY(`coin_info`), " +
							"FOREIGN KEY(`address_fk`) REFERENCES `WalletEntity`(`address`) ON DELETE CASCADE)"
				)

				database.execSQL(
					"ALTER TABLE `TransactionEntity` ADD COLUMN confirm_height INTEGER NOT NULL DEFAULT(0)"
				)

				database.execSQL("ALTER TABLE `TransactionEntity` ADD COLUMN nft_coin_hash TEXT NOT NULL DEFAULT('')")

			}
		}


	}

}
