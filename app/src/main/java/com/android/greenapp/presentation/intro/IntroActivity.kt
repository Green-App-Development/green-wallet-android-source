package com.android.greenapp.presentation.intro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.android.greenapp.R
import com.android.greenapp.presentation.App
import com.android.greenapp.presentation.BaseActivity
import com.android.greenapp.presentation.custom.getStatusBarBcgColorBasedOnTime
import com.android.greenapp.presentation.custom.getStatusBcgBarColorBasedOnTime
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.greeting.GreetingActivity
import com.android.greenapp.presentation.main.MainActivity
import com.example.common.tools.VLog
import com.android.greenapp.presentation.tools.getBooleanResource
import com.android.greenapp.presentation.tools.getColorResource
import kotlinx.coroutines.launch
import javax.inject.Inject

class IntroActivity : BaseActivity() {

    private val navController by lazy { findNavController(R.id.my_nav_host_fragment) }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    val introViewModel: IntroActViewModel by viewModels { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        navigateUser()
        initStatusBarColorRegulation()
        VLog.d("Application is Alive : ${(application as App).applicationIsAlive}")
    }


    private fun initStatusBarColorRegulation() {
        navController.addOnDestinationChangedListener { navController, dest, bundle ->
            when (dest.id) {
                R.id.timeFragment -> {
                    setSystemUiLightStatusBar(isLightStatusBar = getStatusBarBcgColorBasedOnTime())
                    window.statusBarColor = getStatusBcgBarColorBasedOnTime(this)
                }
                R.id.entPasscodeFragment -> {
                    setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
                    window.statusBarColor = getColorResource(R.color.primary_app_background)
                }
                else -> {
                    setSystemUiLightStatusBar(isLightStatusBar = getBooleanResource(R.bool.light_status_bar))
                    window.statusBarColor = getColorResource(R.color.primary_app_background)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setSystemUiLightStatusBar(isLightStatusBar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val systemUiAppearance = if (isLightStatusBar) {
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                } else {
                    0
                }
                window.insetsController?.setSystemBarsAppearance(
                    systemUiAppearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                val systemUiVisibilityFlags = if (isLightStatusBar) {
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                window.decorView.systemUiVisibility = systemUiVisibilityFlags
            }
        }
    }

    private fun navigateUser() {
        lifecycleScope.launch {
            val userUnBoarded = introViewModel.isUserUnBoarded()
            VLog.d("UserBoarded Value : $userUnBoarded")
            val lastTimeVisited = introViewModel.getLastVisitedLongValue()
            if (!userUnBoarded) {
                move2GreetingActivity()
            } else if ((application as App).applicationIsAlive && System.currentTimeMillis() - lastTimeVisited <= 180 * 1000) {
                move2MainActivity()
            }
        }
    }

    fun move2MainActivity() {
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            finish()
        }
    }

    fun move2TimeFragment() {
        navController.navigate(R.id.action_entPasswordFragment_to_timeFragment)
    }

    fun move2EntPasscodeFragment() {

    }

    fun move2GreetingActivity() {
        Intent(this, GreetingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(this)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VLog.d("onDestroy  in IntroActivity")
    }


}