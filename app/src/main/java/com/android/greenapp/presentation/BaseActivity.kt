package com.android.greenapp.presentation

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.ViewPumpAppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.domain.interact.GreenAppInteract
import com.android.greenapp.presentation.custom.ConnectionLiveData
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.main.receiver.NetworkChangeReceiver
import com.example.common.tools.VLog
import dagger.android.support.DaggerAppCompatActivity
import dev.b3nedikt.restring.Restring
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 25.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
open class BaseActivity : DaggerAppCompatActivity() {

    private var appCompatDelegate: AppCompatDelegate? = null

    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    @Inject
    lateinit var dialogMan: DialogManager

    @Inject
    lateinit var greenInteract: GreenAppInteract

    @NonNull
    override fun getDelegate(): AppCompatDelegate {
        if (appCompatDelegate == null) {
            appCompatDelegate = ViewPumpAppCompatDelegate(
                super.getDelegate(),
                this
            ) { base: Context -> Restring.wrapContext(base) }
        }
        return appCompatDelegate as AppCompatDelegate
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Restring.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        monitoringConnection()
    }

    private var langListJob: Job? = null

    private fun monitoringConnection() {

        connectionLiveData.observe(this) {
            VLog.d("Has Internet Now : $it")
            connectionLiveData.isOnline = it
            if (it) {
                dialogMan.dismissNoConnectionDialog()
                langListJob?.cancel()
                langListJob = lifecycleScope.launch {
                    delay(250)
                    greenInteract.getAvailableLanguageList()
                }
            }
        }

    }


}