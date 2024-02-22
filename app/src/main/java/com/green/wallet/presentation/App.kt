package com.green.wallet.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.green.wallet.BuildConfig
import com.green.wallet.R
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SupportInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.custom.NotificationHelper
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.custom.workmanager.WorkManagerSyncTransactions
import com.green.wallet.presentation.di.application.AppComponent
import com.green.wallet.presentation.di.application.DaggerAppComponent
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.SYNC_WORK_TAG
import com.green.wallet.presentation.tools.VLog
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
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
    lateinit var walletInteract: WalletInteract

    @Inject
    lateinit var exchangeInteract: ExchangeInteract

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var tibetInteract: TibetInteract

    lateinit var appComponent: AppComponent

    var applicationIsAlive = false
    var isUserUnBoardDed = false

    private val handler = CoroutineExceptionHandler { context, ex ->
        VLog.d("Caught exception in coroutine scope : ${ex.message} for testing")
    }

    var updateBalanceJob: Job? = null
    private var updateCryptoJob: Job? = null
    private var FLUTTER_ENGINE = "flutter_engine"
    lateinit var flutterEngine: FlutterEngine

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        VLog.d("OnCreate Got Called on App at Local Time : ${System.currentTimeMillis()}")
        quickNavigationIfUserUnBoarded()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Restring.init(this)
        ViewPump.init(RewordInterceptor)
        determineModeAndLanguage()
        updateBalanceEachPeriodically()
        warmupFlutterEngine()
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
        initWorkManager()
        subscribingToTopic()

    }


    private fun subscribingToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("news")
            .addOnCompleteListener {
                VLog.d("On success subscribing to news")
            }.addOnFailureListener {
                VLog.d("On failure subscribing to news")
            }
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            VLog.d("Retrieved token successfully : $it")
        }
    }

    private fun initWorkManager() {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<WorkManagerSyncTransactions>(
                15000,
                TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            SYNC_WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest.build()
        )
    }

    private fun quickNavigationIfUserUnBoarded() {
        CoroutineScope(Dispatchers.IO).launch {
            isUserUnBoardDed = prefs.getSettingBoolean(PrefsManager.USER_UNBOARDED, false)
            val guid = prefs.getSettingString(PrefsManager.USER_GUID, "")
            if (guid.isEmpty()) {
                val generateGuid = UUID.randomUUID().toString().replace("-", "").uppercase()
                prefs.saveSettingString(PrefsManager.USER_GUID, generateGuid)
            }
        }
    }

    lateinit var methodChannel: MethodChannel
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
        VLog.d("LOG_TAG warmupFlutterEngine: got initialized  $flutterEngine")
        methodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL_GENERATE_HASH
        )

        CoroutineScope(Dispatchers.IO + handler).launch {
            //wait for flutter engine to warm up
            delay(500L)
            walletInteract.getAllWalletList().forEach {
                val args = hashMapOf<String, Any>()
                args["mnemonics"] = convertListToStringWithSpace(it.mnemonics)
                args["observer"] = it.observerHash
                args["non_observer"] = it.nonObserverHash
                withContext(Dispatchers.Main) {
                    methodChannel.invokeMethod("initWalletFirstTime", args)
                }
            }
        }

    }

    fun updateBalanceEachPeriodically() {
        updateBalanceJob?.cancel()
        updateBalanceJob = CoroutineScope(Dispatchers.IO + handler).launch {
            while (true) {
                delay(1000L * 60L * 5)
                VLog.d("Start requesting Balance Each Wallets Periodically:")
                blockChainInteract.updateBalanceAndTransactionsPeriodically()
                exchangeInteract.updateOrderStatusPeriodically()
                exchangeInteract.updateTibetSwapExchangeStatus()
                exchangeInteract.updateTibetLiquidityStatus()
            }
        }
        updateCryptoJob?.cancel()
        updateCryptoJob = CoroutineScope(Dispatchers.IO + handler).launch {
            while (true) {
                delay(1000L * 60 * 5)
                VLog.d("Start updating CourseCrypto each Wallets :")
                cryptocurrencyInteract.updateCourseCryptoInDb()
                greenAppInteract.requestOtherNotifItems()
                delay(1000L * 60 * 5)
            }
        }
    }

    fun postPoneAllRequests() {
        updateBalanceJob?.cancel()
        updateCryptoJob?.cancel()
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
        appComponent = DaggerAppComponent.builder().bindApplication(applicationContext).build()
        return appComponent
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    fun isFlutterEngineInitialized() = this::flutterEngine.isInitialized

}
