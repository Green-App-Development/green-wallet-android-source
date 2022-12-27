package com.green.wallet.presentation.main.allsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.common.tools.VLog
import com.green.wallet.R
import com.green.wallet.databinding.FragmentAllSettingsBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.preventDoubleClick
import com.green.wallet.presentation.viewBinding
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class AllSettingsFragment : DaggerDialogFragment() {

    private val binding by viewBinding(FragmentAllSettingsBinding::bind)

    @Inject
    lateinit var factory: ViewModelFactory
    private val settingsViewModel: AllSettingsViewModel by viewModels { factory }

    @Inject
    lateinit var effect: AnimationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VLog.d("onCreate from AllSettingsFragment")
        curActivity().allSettingsDialog = this
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        VLog.d("onCreate View from AllSettingsFragment")
        return inflater.inflate(R.layout.fragment_all_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VLog.d("onView Created from AllSettingsFragment")
        settingViewDetails()
        registerClicks()
    }

    private fun initStatusBarColor() {
        dialog?.apply {
            VLog.d("Changing status bar color on allSettingsFragment")
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = curActivity().getColorResource(R.color.cardView_background)
        }
    }

    override fun onStart() {
        super.onStart()
        VLog.d("onStart from AllSettingsFragment")
        initStatusBarColor()
    }

    override fun onResume() {
        super.onResume()
        VLog.d("onResume from AllSettingsFragment")
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    private fun registerClicks() {
        binding.apply {

            relChangeLanguage.setOnClickListener {
				it.preventDoubleClick()
                curActivity().move2MainLanguageFragment()
            }

            relAboutApp.setOnClickListener {
				it.preventDoubleClick()
				curActivity().move2AboutApp()
            }

            backLayout.setOnClickListener {
				it.preventDoubleClick()
				curActivity().popBackStackOnce()
            }

            btnRadioGroupMode.setOnCheckedChangeListener { p0, p1 ->
                val nightMode = btnNightMode.isChecked
                settingsViewModel.saveNightModeIsOn(nightMode)
            }

            btnPushNotifSwitch.setOnCheckedChangeListener { p0, p1 ->
                settingsViewModel.updatePushNotifIsOn(
                    p1
                )
            }

            btnHideBalanceSwitch.setOnCheckedChangeListener { p0, p1 ->
                settingsViewModel.updateHideBalanceIsOn(
                    p1
                )
            }

            relSupportCall.setOnClickListener {
				it.preventDoubleClick()
				curActivity().move2SupportFragmentFromAllSettingsDialog()
            }

            relNotif.setOnClickListener {
				it.preventDoubleClick()
				curActivity().move2NotificationFragment()
            }

        }


    }


    private fun settingViewDetails() {
        binding.apply {
            lifecycleScope.launch {
                val nightMode = settingsViewModel.getNightModeIsOn()
                if (nightMode) {
                    btnNightMode.isChecked = true
                } else
                    btnLightMode.isChecked = true
                btnPushNotifSwitch.isChecked = settingsViewModel.getPushNotifIsOn()
                btnHideBalanceSwitch.isChecked = settingsViewModel.getHideBalanceIsOn()
            }
        }
    }

    private fun curActivity() = requireActivity() as MainActivity


}
