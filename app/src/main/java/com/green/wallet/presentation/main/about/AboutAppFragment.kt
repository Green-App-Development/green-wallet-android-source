package com.green.wallet.presentation.main.about

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentAboutAppBinding
import dagger.android.support.DaggerDialogFragment
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.viewBinding

/**
 * Created by bekjan on 11.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class AboutAppFragment : DaggerDialogFragment() {

    private val binding by viewBinding(FragmentAboutAppBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_app, container, false)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerClicks()
        settingViewDetails()
        initStatusBarColor()
    }

    private fun initStatusBarColor() {
        dialog?.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = curActivity().getColorResource(R.color.status_bar_color_send)
        }
    }



    private fun settingViewDetails() {
        binding.apply {
            txtAboutApp.movementMethod = ScrollingMovementMethod()
        }
    }

    private fun registerClicks() {
        binding.apply {

            backLayout.setOnClickListener {
                curActivity().popBackStackOnce()
            }


        }
    }

    private fun curActivity() = requireActivity() as MainActivity


}
