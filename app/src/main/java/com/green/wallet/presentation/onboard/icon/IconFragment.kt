package com.green.wallet.presentation.onboard.icon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentIconBinding
import com.example.common.tools.VLog
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.onboard.OnBoardActivity
import com.green.wallet.presentation.onboard.OnBoardViewModel
import com.green.wallet.presentation.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class IconFragment : DaggerFragment() {

    private val binding by viewBinding(FragmentIconBinding::bind)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val greetingViewModel: OnBoardViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var effect: AnimationManager

    @Inject
    lateinit var gson: Gson


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VLog.d("GreetingViewModel  IconFragment : $greetingViewModel")
        greetingViewModel.saveAppInstallTime(System.currentTimeMillis())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_icon, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerButtons()
        changeAppIcon()
    }


    private fun changeAppIcon() {

    }


    private fun registerButtons() {
        changeVisibilityBtns()
        binding.btnStart.setOnClickListener {
            it.startAnimation(effect.getBtnEffectAnimation())
            if (curActivity().reset_app_clicked) {
                curActivity().move2TermsFragment()
            } else
                curActivity().move2LanguageFragment()
        }

    }

    private fun changeVisibilityBtns() {
        lifecycleScope.launch {
            delay(2000)
            binding.btnStart.visibility = View.VISIBLE
        }
    }

    private fun curActivity() = requireActivity() as OnBoardActivity


}
