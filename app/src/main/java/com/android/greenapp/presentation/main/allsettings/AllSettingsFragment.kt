package com.android.greenapp.presentation.main.allsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentAllSettingsBinding
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.VLog
import com.android.greenapp.presentation.tools.getColorResource
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
        initStatusBarColor()
    }

    private fun initStatusBarColor() {
        dialog?.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = curActivity().getColorResource(R.color.status_bar_color_send)
        }
    }

    override fun onStart() {
        super.onStart()
        VLog.d("onStart from AllSettingsFragment")
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
                curActivity().move2MainLanguageFragment()
            }

            relAboutApp.setOnClickListener {
                curActivity().move2AboutApp()
            }

            backLayout.setOnClickListener {
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
                curActivity().move2SupportFragmentFromAllSettingsDialog()
            }

            relNotif.setOnClickListener {
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