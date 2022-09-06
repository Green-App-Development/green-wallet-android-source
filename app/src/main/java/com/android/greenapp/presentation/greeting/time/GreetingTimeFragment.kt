package com.android.greenapp.presentation.greeting.time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentTimeBinding
import com.android.greenapp.presentation.greeting.GreetingActivity
import com.android.greenapp.presentation.tools.getColorResource
import com.android.greenapp.presentation.tools.getDrawableResource
import com.android.greenapp.presentation.tools.getStringResource
import com.example.common.tools.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GreetingTimeFragment : Fragment(R.layout.fragment_time) {

    private val handler = CoroutineExceptionHandler { con, thro ->
        VLog.d("TImeFragment Exception Handled : ${thro.message}")
    }

    lateinit var binding: FragmentTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingBcgImageResource()
        changeGreetingTime()
        applyExtraBoldTypeFace()
        lifecycleScope.launch(handler) {
            delay(3000)
            curActivity().move2MainActivity()
        }
    }

    private fun changeGreetingTime() {
        val curMinutes = getCurrentTimeInMinutes()
        val greetingTime = when {
            curMinutes in 0..com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE -> curActivity().getStringResource(R.string.welcome_screen_titel_night)
            curMinutes in com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE..com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE -> curActivity().getStringResource(
                R.string.welcome_screen_titel_morning
            )
            curMinutes in com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE..com.android.greenapp.presentation.tools.EVENING_STARTS_MINUTE -> curActivity().getStringResource(
                R.string.welcome_screen_titel_afternoon
            )
            else -> curActivity().getStringResource(R.string.welcome_screen_titel_evening)
        }
        binding.txtGreeting.text = greetingTime
    }

    private fun applyExtraBoldTypeFace() {
        binding.txtGreeting.apply {
            typeface = ResourcesCompat.getFont(curActivity(), R.font.inter_exra_bold)
        }
        val curMinutes = getCurrentTimeInMinutes()
        if (curMinutes in com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE..com.android.greenapp.presentation.tools.EVENING_STARTS_MINUTE)
            binding.txtGreeting.setTextColor(curActivity().getColorResource(R.color.lighter_black))
    }

    private fun settingBcgImageResource() {
        binding.rootConstraint.background =
            curActivity().getDrawableResource(getMatchedImageResource())
    }

    private fun getMatchedImageResource(): Int {
        val curMinutes = getCurrentTimeInMinutes()
        VLog.d("CurMinutes : $curMinutes")
        var res = when {
            curMinutes in 0..com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE -> R.drawable.good_night
            curMinutes in com.android.greenapp.presentation.tools.MORNING_STARTS_MINUTE..com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE -> R.drawable.good_morning
            curMinutes in com.android.greenapp.presentation.tools.NOON_STARTS_MINUTE..com.android.greenapp.presentation.tools.EVENING_STARTS_MINUTE -> R.drawable.good_day
            else -> R.drawable.good_evening
        }
        return res
    }


    private fun curActivity() = requireActivity() as GreetingActivity

}