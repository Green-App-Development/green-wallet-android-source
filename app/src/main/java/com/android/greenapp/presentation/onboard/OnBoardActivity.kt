package com.android.greenapp.presentation.onboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.android.greenapp.R
import com.android.greenapp.databinding.ActivityGreetingBinding
import com.android.greenapp.presentation.BaseActivity
import com.android.greenapp.presentation.custom.getStatusBarBcgColorBasedOnTime
import com.android.greenapp.presentation.custom.getStatusBcgBarColorBasedOnTime
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.intro.IntroActivity
import com.android.greenapp.presentation.main.MainActivity
import com.example.common.tools.*
import java.util.*
import javax.inject.Inject

class OnBoardActivity : BaseActivity() {

    lateinit var binding: ActivityGreetingBinding
    private val navController by lazy { findNavController(R.id.my_nav_host_fragment) }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    val greetingViewModel: OnBoardViewModel by viewModels { viewModelFactory }


    companion object {
        const val RESET_APP_CLICKED = "reset_app_clicked"
    }

    var reset_app_clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGreetingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initStatusBarColorRegulation()
        intent.extras?.let {
            reset_app_clicked = it.getBoolean(RESET_APP_CLICKED, false)
        }
        VLog.d("Clearing cache btn clicked : $reset_app_clicked")
    }

    private fun initStatusBarColorRegulation() {
        navController.addOnDestinationChangedListener { navController, dest, bundle ->
            when (dest.id) {
                R.id.timeFragment2 -> {
                    setSystemUiLightStatusBar(isLightStatusBar = getStatusBarBcgColorBasedOnTime())
                    window.statusBarColor = getStatusBcgBarColorBasedOnTime(this)
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


    fun move2LanguageFragment() {
        navController.navigate(R.id.action_themeFragment_to_languageFragment2)
    }

    fun move2TermsFragment() {
        navController.navigate(R.id.termsFragment)
    }

    fun move2BackToLanguageFragment() {
        navController.navigate(R.id.action_termsFragment_to_languageFragment2)
    }

    fun move2SetPasswordFragment() {
        navController.navigate(R.id.action_termsFragment_to_setPasswordFragment)
    }

    fun move2IntroActivity() {
        Intent(this, IntroActivity::class.java).apply {
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(this)
        }
    }

    fun move2MainActivity() {
        Intent(this@OnBoardActivity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(this)
            finish()
        }
    }

    fun move2TimeFragment() {
        navController.navigate(R.id.action_setPasswordFragment_to_timeFragment2)
    }

    fun popBackStack() {
        navController.popBackStack()
    }

    fun move2BackTermsFragment() {
        navController.navigate(R.id.action_setPasswordFragment_to_termsFragment)
    }

    fun move2LanguageListFragment() {
        navController.navigate(R.id.languageFragment2)
    }

    override fun onDestroy() {
        super.onDestroy()

    }


}