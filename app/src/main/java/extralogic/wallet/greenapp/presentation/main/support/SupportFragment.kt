package extralogic.wallet.greenapp.presentation.main.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentSupportBinding
import extralogic.wallet.greenapp.presentation.custom.AnimationManager
import extralogic.wallet.greenapp.presentation.main.MainActivity
import extralogic.wallet.greenapp.presentation.tools.getColorResource
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class SupportFragment : DaggerDialogFragment() {

	private lateinit var binding: FragmentSupportBinding

	@Inject
	lateinit var effect: AnimationManager


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSupportBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerClicks()
		initStatusBarColor()
	}

	private fun registerClicks() {
		binding.apply {
			backLayout.setOnClickListener {
				curActivity().popBackStackOnce()
			}

			relFaq.setOnClickListener {
				curActivity().move2FaqFragment()
			}

			relAskQuestion.setOnClickListener {
				curActivity().move2AskQuestionFragment()
			}

			relListing.setOnClickListener {
				curActivity().move2ListingFragment()
			}

			relAboutApp.setOnClickListener {
				curActivity().move2AboutApp()
			}

		}
	}

	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.primary_app_background)
		}
	}


	private fun curActivity() = requireActivity() as MainActivity

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}


}
