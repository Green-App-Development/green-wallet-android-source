package com.green.wallet.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.green.wallet.BuildConfig
import com.green.wallet.R
import com.example.common.tools.VLog
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.custom.NotificationHelper
import com.green.wallet.presentation.custom.workmanager.WorkManagerSyncTransactions
import com.green.wallet.presentation.di.application.DaggerAppComponent
import com.green.wallet.presentation.tools.SYNC_WORK_TAG
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class App : DaggerApplication() {

	@Inject
	lateinit var blockChainInteract: BlockChainInteract

	@Inject
	lateinit var cryptocurrencyInteract: CryptocurrencyInteract

	@Inject
	lateinit var supportInteract: SupportInteract

	@Inject
	lateinit var prefs: PrefsInteract

	@Inject
	lateinit var greenAppInteract: GreenAppInteract


	@Inject
	lateinit var workerFactory: WorkerFactory

	@Inject
	lateinit var notificationHelper: NotificationHelper


	var applicationIsAlive = false
	var isUserUnBoardDed = true

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
		quickNavigationIfUserUnBoarded()
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		Restring.init(this)
		ViewPump.init(RewordInterceptor)
		determineModeAndLanguage()
		requestsPerApplication()
		updateBalanceEachPeriodically()
		if (BuildConfig.DEBUG)
			Timber.plant(Timber.DebugTree())
		warmupFlutterEngine()
		WorkManager.initialize(
			this,
			Configuration.Builder().setWorkerFactory(workerFactory).build()
		)
		initWorkManager()

	}

	private fun requestsPerApplication() {
		CoroutineScope(Dispatchers.IO).launch {
			with(greenAppInteract) {
				getAvailableNetworkItemsFromRestAndSave()
				getAvailableLanguageList()
				getAgreementsText()
			}
			supportInteract.getFAQQuestionAnswers()
		}
	}

	private fun initWorkManager() {
		val constraints =
			Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
		val periodicWorkRequest =
			PeriodicWorkRequestBuilder<WorkManagerSyncTransactions>(15000, TimeUnit.MILLISECONDS)
				.setConstraints(constraints)
		WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
			SYNC_WORK_TAG,
			ExistingPeriodicWorkPolicy.REPLACE,
			periodicWorkRequest.build()
		)
	}

		private fun testingMethod() {
		CoroutineScope(Dispatchers.IO).launch {
			for (i in 0 until 10) {
				VLog.d("Waiting : $i")
				delay(1000L)
			}
		}
	}

	private fun quickNavigationIfUserUnBoarded() {
		CoroutineScope(Dispatchers.IO).launch {
			isUserUnBoardDed = prefs.getSettingBoolean(PrefsManager.USER_UNBOARDED, true)
		}
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


	fun updateBalanceEachPeriodically() {
		updateBalanceJob?.cancel()
		updateBalanceJob = CoroutineScope(Dispatchers.IO + handler).launch {
			while (true) {
				VLog.d("Start requesting Balance Each Wallets Periodically:")
				blockChainInteract.updateBalanceAndTransactionsPeriodically()
				delay(1000 * 30L)
			}
		}
		updateCryptoJob?.cancel()
		updateCryptoJob = CoroutineScope(Dispatchers.IO + handler).launch {
			greenAppInteract.getAvailableNetworkItemsFromRestAndSave()
			cryptocurrencyInteract.getAllTails()
			cryptocurrencyInteract.checkingDefaultWalletTails()
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

	fun isFlutterEngineInitialized() = this::flutterEngine.isInitialized


}
