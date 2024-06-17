package com.green.wallet.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.green.wallet.data.local.entity.*

@Database(
    entities = [
        AddressEntity::class,
        WalletEntity::class,
        TransactionEntity::class,
        NotifOtherEntity::class,
        TokenEntity::class,
        SpentCoinsEntity::class,
        FaqItemEntity::class,
        NFTInfoEntity::class,
        NFTCoinEntity::class,
        OrderEntity::class,
        TibetSwapEntity::class,
        TibetLiquidityEntity::class,
        OfferTransactionEntity::class,
        CancelTransactionEntity::class
    ],
    version = 38,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 25, to = 34),
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
    abstract val orderExchangeDao: OrderExchangeDao
    abstract val tibetDao: TibetDao
    abstract val offerTransDao: OfferTransactionDao
    abstract val cancelTranDao: CancelTransactionDao

    companion object {

        const val APP_DB_NAME = "green_app_database_name"

        val migration25To34 = object : Migration(25, 34) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
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
                            "`isPending` INTEGER NOT NULL," +
                            "PRIMARY KEY(`nft_coin_hash`), " +
                            "FOREIGN KEY(`address_fk`) REFERENCES `WalletEntity`(`address`) ON DELETE CASCADE)"
                )

                db.execSQL(
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

                db.execSQL(
                    "ALTER TABLE `TransactionEntity` ADD COLUMN confirm_height INTEGER NOT NULL DEFAULT(0)"
                )

                db.execSQL("ALTER TABLE `TransactionEntity` ADD COLUMN nft_coin_hash TEXT NOT NULL DEFAULT('')")

            }
        }


        val migration34To35 = object : Migration(34, 35) {

            override fun migrate(db: SupportSQLiteDatabase) {
                val createTableQuery = """
            CREATE TABLE IF NOT EXISTS OrderEntity (
                order_hash TEXT PRIMARY KEY NOT NULL,
                status TEXT NOT NULL,
                amount_to_send REAL NOT NULL,
                give_address TEXT NOT NULL,
                time_created INTEGER NOT NULL,
                rate REAL NOT NULL,
                send_coin TEXT NOT NULL,
                get_coin TEXT NOT NULL,
                get_address TEXT NOT NULL,
                tx_ID TEXT NOT NULL,
                commission_fee REAL NOT NULL, 
				amount_to_receive REAL NOT NULL,
				commission_tron REAL NOT NULL,
				commission_percent REAL NOT NULL,
				expired_cancelled_time INTEGER NOT NULL
            )
        """.trimIndent()
                db.execSQL(createTableQuery)
            }

        }

        val migration35To36 = object : Migration(35, 36) {

            override fun migrate(db: SupportSQLiteDatabase) {
                val addColumn = """
           ALTER TABLE TokenEntity ADD COLUMN pair_id TEXT NOT NULL DEFAULT ''
        """.trimIndent()
                db.execSQL(addColumn)

                val createTableTibetSwap = """
            CREATE TABLE IF NOT EXISTS TibetSwapEntity (
                offer_id TEXT PRIMARY KEY NOT NULL,
				send_amount REAL NOT NULL,
                receive_amount REAL NOT NULL,
                send_coin TEXT NOT NULL,
                receive_coin TEXT NOT NULL,
                fee REAL NOT NULL,
                time_created INTEGER NOT NULL,
                status TEXT NOT NULL,
				height INTEGER NOT NULL,
				fk_address TEXT NOT NULL
            )
        """.trimIndent()
                db.execSQL(createTableTibetSwap)


                val createTableTibetLiquidity = """
            CREATE TABLE IF NOT EXISTS TibetLiquidityEntity (
                offer_id TEXT PRIMARY KEY NOT NULL,
				xchAmount REAL NOT NULL,
                catAmount REAL NOT NULL,
                catToken TEXT NOT NULL,
				liquidityAmount REAL NOT NULL,
				liquidityToken TEXT NOT NULL,
                fee REAL NOT NULL,
                time_created INTEGER NOT NULL,
                status TEXT NOT NULL,
				height INTEGER NOT NULL,
				addLiquidity INTEGER NOT NULL,
				fk_address TEXT NOT NULL
            )
        """.trimIndent()
                db.execSQL(createTableTibetLiquidity)
            }

        }

        val migration6To37 = object : Migration(36, 37) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val addColumn = """
           ALTER TABLE WalletEntity ADD COLUMN encrypt_stage INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
                db.execSQL(addColumn)
            }
        }


        val migration37To38 = object : Migration(37, 38) {

            override fun migrate(db: SupportSQLiteDatabase) {
                val createTableQuery = """
            CREATE TABLE IF NOT EXISTS OfferTransactionEntity (
                tranId TEXT PRIMARY KEY NOT NULL,
                status TEXT NOT NULL,
                addressFk TEXT NOT NULL,
                requested TEXT NOT NULL,
                offered TEXT NOT NULL,
                acceptOffer INTEGER NOT NULL,
                source TEXT NOT NULL,
                createdTime INTEGER NOT NULL,
                hashTransaction TEXT NOT NULL,
                fee REAL NOT NULL,
                height INTEGER NOT NULL,
                cancelled INTEGER NOT NULL,
                FOREIGN KEY(addressFk) REFERENCES WalletEntity(address) ON DELETE CASCADE
            )
        """.trimIndent()

                db.execSQL(
                    "ALTER TABLE `NFTInfoEntity` ADD COLUMN timePending INTEGER NOT NULL DEFAULT(0)"
                )

                db.execSQL(createTableQuery)

                val createCancelTranTable = """
            CREATE TABLE IF NOT EXISTS CancelTransactionEntity (
                offerTranID TEXT PRIMARY KEY NOT NULL,
                addressFk TEXT NOT NULL,
                createAtTime INTEGER NOT NULL,
                FOREIGN KEY(addressFk) REFERENCES WalletEntity(address) ON DELETE CASCADE
            )
        """.trimIndent()

                db.execSQL(createCancelTranTable)
            }

        }


    }

}
