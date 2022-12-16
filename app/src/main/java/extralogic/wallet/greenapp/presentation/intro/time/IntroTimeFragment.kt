package extralogic.wallet.greenapp.presentation.intro.time

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentTimeBinding
import extralogic.wallet.greenapp.presentation.intro.IntroActivity
import com.example.common.tools.VLog
import extralogic.wallet.greenapp.presentation.tools.getColorResource
import extralogic.wallet.greenapp.presentation.tools.getDrawableResource
import extralogic.wallet.greenapp.presentation.tools.getStringResource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class IntroTimeFragment : Fragment(R.layout.fragment_time) {

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
            curActivity().introViewModel.saveLastVisitedLongValue(System.currentTimeMillis())
            curActivity().move2MainActivity()
        }
    }

    private fun changeGreetingTime() {
        val curMinutes = getCurrentTimeInMinutes()
        val greetingTime = when {
            curMinutes in 0..MORNING_STARTS_MINUTE -> curActivity().getStringResource(R.string.welcome_screen_titel_night)
            curMinutes in MORNING_STARTS_MINUTE..NOON_STARTS_MINUTE -> curActivity().getStringResource(
                R.string.welcome_screen_titel_morning
            )
            curMinutes in NOON_STARTS_MINUTE..EVENING_STARTS_MINUTE -> curActivity().getStringResource(
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
        if (curMinutes in NOON_STARTS_MINUTE..EVENING_STARTS_MINUTE)
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
            curMinutes in 0..MORNING_STARTS_MINUTE -> R.drawable.good_night
            curMinutes in MORNING_STARTS_MINUTE..NOON_STARTS_MINUTE -> R.drawable.good_morning
            curMinutes in NOON_STARTS_MINUTE..EVENING_STARTS_MINUTE -> R.drawable.good_day
            else -> R.drawable.good_evening
        }
        return res
    }

    private val NIGHT_STARTS_MINUTE = 22 * 60
    private val MORNING_STARTS_MINUTE = 6 * 60
    private val NOON_STARTS_MINUTE = 12 * 60
    private val EVENING_STARTS_MINUTE = 18 * 60

    fun getCurrentTimeInMinutes(): Int {
        val dateFormat = SimpleDateFormat("HH:mm")
        val time = dateFormat.format(Date(System.currentTimeMillis()))
        val split = time.split(":")
        val minute = Integer.valueOf(split[1])
        return Integer.valueOf(split[0]) * 60 + minute
    }

    private fun curActivity() = requireActivity() as IntroActivity

}