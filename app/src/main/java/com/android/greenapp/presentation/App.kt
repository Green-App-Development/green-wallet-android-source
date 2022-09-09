package com.android.greenapp.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import cash.z.ecc.android.bip39.Mnemonics
import com.android.greenapp.BuildConfig
import com.android.greenapp.R
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.*
import com.android.greenapp.presentation.custom.NotificationHelper
import com.android.greenapp.presentation.di.application.DaggerAppComponent
import com.example.common.tools.VLog
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import kotlinx.coroutines.*
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
		VLog.d("OnCreate Got Called on App")
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


	}

	private fun oneTimeRequest() {
		CoroutineScope(Dispatchers.IO +handler).launch {
			greenAppInteract.updateCoinDetails()
			cryptocurrencyInteract.getAllTails()
			greenAppInteract.requestOtherNotifItems()
			val curLangCode = prefs.getSettingString(PrefsManager.CUR_LANGUAGE_CODE, "")
			if (curLangCode.isNotEmpty()) {
				greenAppInteract.downloadLanguageTranslate(curLangCode)
//				greenAppInteract.changeLanguageIsSavedBefore()
			}
		}
	}


	private fun generate12WordMnemonics(): MutableList<String> {
		val mnemonicCode =
			Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12).words.map { String(it) }.toSet()
		if (mnemonicCode.size < 12)
			return generate12WordMnemonics()
		return mnemonicCode.toMutableList()
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
		updateBalanceJob?.cancel()
		updateCryptoJob?.cancel()
	}


}
