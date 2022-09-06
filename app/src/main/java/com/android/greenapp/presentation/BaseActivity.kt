package com.android.greenapp.presentation

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.ViewPumpAppCompatDelegate
import dagger.android.support.DaggerAppCompatActivity
import dev.b3nedikt.restring.Restring

/**
 * Created by bekjan on 25.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
open class BaseActivity : DaggerAppCompatActivity() {

    private var appCompatDelegate: AppCompatDelegate? = null

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


}