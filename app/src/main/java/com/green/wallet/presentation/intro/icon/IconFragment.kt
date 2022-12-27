package com.green.wallet.presentation.intro.icon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.green.wallet.R
import com.green.wallet.databinding.FragmentIconBinding
import com.green.wallet.presentation.intro.IntroActivity
import com.green.wallet.presentation.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IconFragment : Fragment() {


    private val binding by viewBinding(FragmentIconBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_icon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeAppIcon()
        navigateUserToPasscodeFragment()
    }

    private fun navigateUserToPasscodeFragment() {
        lifecycleScope.launch {
            delay(2000)
            curActivity().move2EntPasscodeFragment()
        }
    }

    private fun changeAppIcon() {
        lifecycleScope.launch {
            val nightMode = curActivity().introViewModel.getNightModeIsOn()
            binding.imgIcon.setImageResource(if (nightMode) R.drawable.green_ic_night else R.drawable.green_ic_light)
        }
    }

    private fun curActivity() = requireActivity() as IntroActivity

}
