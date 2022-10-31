package com.android.greenapp.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.android.greenapp.BuildConfig
import com.android.greenapp.R
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.*
import com.android.greenapp.presentation.custom.NotificationHelper
import com.android.greenapp.presentation.custom.workmanager.WorkManagerSyncTransactions
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class App : DaggerApplication() {

	@Inject
	lateinit var blockChainInteract: BlockChainInteract

	@Inject
	lateinit var cryptocurrencyInteract: CryptocurrencyInteract

	@Inject
	lateinit var prefs: PrefsInteract

	@Inject
	lateinit var greenAppInteract: GreenAppInteract


	@Inject
	lateinit var workerFactory: WorkerFactory


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
		updateBalanceEachPeriodically()
		if (BuildConfig.DEBUG)
			Timber.plant(Timber.DebugTree())
		warmupFlutterEngine()
		WorkManager.initialize(
			this,
			Configuration.Builder().setWorkerFactory(workerFactory).build()
		)
		initWorkManager()
		tetingMethod()
	}

	private fun tetingMethod() {
		val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:SS")
		val dateString = formatter.format(Date(1667192560000L))
		VLog.d("Date String  : $dateString")
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

	private fun initWorkManager() {
		val constraints =
			Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
		val periodicWorkRequest =
			PeriodicWorkRequestBuilder<WorkManagerSyncTransactions>(1000, TimeUnit.MILLISECONDS)
				.setConstraints(constraints)
		WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
			"syncWorker",
			ExistingPeriodicWorkPolicy.REPLACE,
			periodicWorkRequest.build()
		)
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
