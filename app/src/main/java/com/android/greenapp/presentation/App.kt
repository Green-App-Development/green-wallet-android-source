package com.android.greenapp.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.android.greenapp.BuildConfig
import com.android.greenapp.R
import com.android.greenapp.data.network.BlockChainService
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.*
import com.android.greenapp.presentation.custom.NotificationHelper
import com.android.greenapp.presentation.di.application.DaggerAppComponent
import com.android.greenapp.presentation.main.send.spend.Coin
import com.android.greenapp.presentation.main.send.spend.CoinSpend
import com.android.greenapp.presentation.main.send.spend.SpenBunde
import com.android.greenapp.presentation.main.send.spend.SpendBundle
import com.android.greenapp.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.example.common.tools.VLog
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject


class App : DaggerApplication() {

	@Inject
	lateinit var blockChainInteract: BlockChainInteract

	@Inject
	lateinit var cryptocurrencyInteract: CryptocurrencyInteract

	@Inject
	lateinit var transactionInteract: TransactionInteract

	@Inject
	lateinit var retrofitBuilder: Retrofit.Builder

	@Inject
	lateinit var prefs: PrefsInteract

	@Inject
	lateinit var greenAppInteract: GreenAppInteract

	@Inject
	lateinit var blockchainInteract: BlockChainInteract

	@Inject
	lateinit var notifHelper: NotificationHelper

	@Inject
	lateinit var notificationHelper: NotificationHelper

	var applicationIsAlive = false

	private val handler = CoroutineExceptionHandler { context, ex ->
		VLog.d("Caught exception in coroutine scope : ${ex.message} for testing")
	}


	var updateBalanceJob: Job? = null
	private var updateCryptoJob: Job? = null
	private var FLUTTER_ENGINE = "flutter_engine"
	lateinit var flutterEngine: FlutterEngine

	@RequiresApi(Build.VERSION_CODES.N)
	override fun onCreate() {
		super.onCreate()
		VLog.d("OnCreate Got Called on App 1")
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		Restring.init(this)
		ViewPump.init(RewordInterceptor)
		determineModeAndLanguage()
		updateBalanceEachPeriodically()
		if (BuildConfig.DEBUG)
			Timber.plant(Timber.DebugTree())
		oneTimeRequest()
		warmupFlutterEngine()
	}

	private fun warmupFlutterEngine() {
		flutterEngine = FlutterEngine(this)

		// Start executing Dart code to pre-warm the FlutterEngine.
		flutterEngine.dartExecutor.executeDartEntrypoint(
			DartExecutor.DartEntrypoint.createDefault()
		)

		// Cache the FlutterEngine to be used by FlutterActivity.
		FlutterEngineCache
			.getInstance()
			.put(FLUTTER_ENGINE, flutterEngine)
		VLog.d("LOG_TAG", "warmupFlutterEngine: got initialized  $flutterEngine")
//		testingMethod()
	}

	private fun testingMethod() {
		val methodChannel = MethodChannel(
			flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		methodChannel.setMethodCallHandler { method, callBack ->
			if (method.method == "getSpendBundle") {
				val spendBundleFlutter =
					(method.arguments as HashMap<*, *>)["spendBundle"].toString()
				VLog.d("Got spend bundle on send fragment : $spendBundleFlutter")

				val spendBundleJson = JSONObject(spendBundleFlutter)
				val agg_signature = spendBundleJson.getString("aggregated_signature")

				val coinSpends = mutableListOf<CoinSpend>()
				val coinSpendsSize = spendBundleJson.getJSONArray("coin_spends").length()

				for (i in 0 until coinSpendsSize) {
					val coin_spend =
						JSONObject(spendBundleJson.getJSONArray("coin_spends")[i].toString())

					val puzzle_reveal = coin_spend.getString("puzzle_reveal")
					val solution = coin_spend.getString("solution")
					val coinJSON = JSONObject(coin_spend.get("coin").toString())
					val parent_coin_info = coinJSON.getString("parent_coin_info")
					val puzzle_hash = coinJSON.getString("puzzle_hash")
					val amount = coinJSON.getLong("amount")
					val coin = Coin(amount, parent_coin_info, puzzle_hash)
					val coinSpend = CoinSpend(coin, puzzle_reveal, solution)

					coinSpends.add(coinSpend)
				}
				val spendBundle = SpendBundle(agg_signature, coinSpends.toList())
				val spenBundle = SpenBunde(spendBundle)
				VLog.d("SpendBundle Sending to server push_tx : $spenBundle")
				CoroutineScope(Dispatchers.IO).launch {
					val curBlockChainService =
						retrofitBuilder.baseUrl("https://chia.blockchain-list.store/full-node/")
							.build()
							.create(BlockChainService::class.java)

					val res = curBlockChainService.pushTransaction(spenBundle)
					VLog.d("Result from push_transaction  ${res.body()} : ${res.body()!!.status}")
				}
			}
		}
	}

	private fun oneTimeRequest() {
//		CoroutineScope(Dispatchers.IO +handler).launch {
//			greenAppInteract.updateCoinDetails()
//			cryptocurrencyInteract.getAllTails()
//			greenAppInteract.requestOtherNotifItems()
//			val curLangCode = prefs.getSettingString(PrefsManager.CUR_LANGUAGE_CODE, "")
//			if (curLangCode.isNotEmpty()) {
//				greenAppInteract.downloadLanguageTranslate(curLangCode)
//			}
//		}
	}


	fun updateBalanceEachPeriodically() {
		updateBalanceJob?.cancel()
		updateBalanceJob = CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				VLog.d("Start requesting Balance Each Wallets Periodically:")
				blockChainInteract.updateBalanceAndTransactionsPeriodically()
				delay(1000 * 30L)
			}
		}
		updateCryptoJob?.cancel()
		updateCryptoJob = CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				VLog.d("Start updating CourseCrypto each Wallets :")
				cryptocurrencyInteract.updateCourseCryptoInDb()
				greenAppInteract.requestOtherNotifItems()
				delay(1000L * 60)
			}
		}
	}


	private fun determineModeAndLanguage() {
		CoroutineScope(Dispatchers.Main).launch {
			launch {
				val checkStringHasValue = getString(R.string.ready_btn)
				VLog.d("Check string has value of ready_btn on App : $checkStringHasValue")
				greenAppInteract.changeLanguageIsSavedBefore()
			}
			launch {
				val nightMode =
					prefs.getSettingBoolean(PrefsManager.NIGHT_MODE_ON, default = true)
				withContext(Dispatchers.Main) {
					AppCompatDelegate.setDefaultNightMode(if (nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
				}
			}
		}
	}

	override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
		return DaggerAppComponent.builder().bindApplication(applicationContext).build()
	}

	override fun onLowMemory() {
		super.onLowMemory()
	}


}
