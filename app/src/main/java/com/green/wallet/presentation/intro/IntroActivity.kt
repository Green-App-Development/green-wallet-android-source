package com.green.wallet.presentation.intro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.green.wallet.R
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.App
import com.green.wallet.presentation.BaseActivity
import com.green.wallet.presentation.custom.getStatusBarBcgColorBasedOnTime
import com.green.wallet.presentation.custom.getStatusBcgBarColorBasedOnTime
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.MainActivity.Companion.MAIN_BUNDLE_KEY
import com.green.wallet.presentation.onboard.OnBoardActivity
import com.green.wallet.presentation.tools.getBooleanResource
import com.green.wallet.presentation.tools.getColorResource
import kotlinx.coroutines.launch
import javax.inject.Inject

class IntroActivity : BaseActivity() {

	private val navController by lazy { findNavController(R.id.my_nav_host_fragment) }

	companion object {
		const val INTRO_BUNDLE_KEY = "intro_bundle"
	}

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	val introViewModel: IntroActViewModel by viewModels { viewModelFactory }


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		navigateUser()
		setContentView(R.layout.activity_intro)
		initStatusBarColorRegulation()
		VLog.d("Application is Alive : ${(application as App).applicationIsAlive}")
		checkingIntentFromPush()
	}


	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		VLog.d(
			"Open with push notification message new intent : ${
				intent?.getBundleExtra(
					INTRO_BUNDLE_KEY
				)
			}"
		)

	}

	private fun checkingIntentFromPush() {
		val introBundle = intent.getBundleExtra(INTRO_BUNDLE_KEY)
		if (introBundle == null) {
			VLog.d("Intro Bundle is null on intro activity")
			return
		}
		introBundle.keySet().forEach {
			VLog.d("IntroBundle on intro activity key : $it -> value : ${introBundle.getString(it)}")
		}
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
		if (!(application as App).isUserUnBoardDed) {
			move2OnboardingActivity(false)
			return
		}
		lifecycleScope.launch {
			val userUnBoarded = introViewModel.isUserUnBoarded()
			VLog.d("UserBoarded Value : $userUnBoarded")
			if (!userUnBoarded) {
				move2OnboardingActivity(false)
			}
			val lastTimeVisited = introViewModel.getLastVisitedLongValue()
			if ((application as App).applicationIsAlive && System.currentTimeMillis() - lastTimeVisited <= 180 * 1000) {
				move2MainActivity()
			}
		}
	}

	fun move2MainActivity() {
		Intent(this, MainActivity::class.java).apply {
			addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
			putExtra(MAIN_BUNDLE_KEY, intent.getBundleExtra(INTRO_BUNDLE_KEY))
			startActivity(this)
			finish()
		}
	}

	fun move2TimeFragment() {
		navController.navigate(R.id.action_entPasswordFragment_to_timeFragment)
	}

	fun move2EntPasscodeFragment() {

	}


	fun move2OnboardingActivity(reset_app_clicked: Boolean) {
		Intent(this, OnBoardActivity::class.java).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
			putExtra(OnBoardActivity.RESET_APP_CLICKED, reset_app_clicked)
			startActivity(this)
			finish()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		VLog.d("onDestroy  in IntroActivity")
	}


}
