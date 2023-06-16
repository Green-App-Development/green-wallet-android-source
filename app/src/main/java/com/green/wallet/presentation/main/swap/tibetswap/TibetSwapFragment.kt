package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTibetswapBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.dpFromPx
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.send.SwapSendViewModel
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetSwapFragment : DaggerFragment() {

	private lateinit var binding: FragmentTibetswapBinding

	@Inject
	lateinit var animManager: AnimationManager

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetSwapViewModel by viewModels { viewModelFactory }


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentTibetswapBinding.inflate(layoutInflater)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.prepareRelChosen()
	}


	private fun FragmentTibetswapBinding.prepareRelChosen() {
		relChosen.viewTreeObserver.addOnGlobalLayoutListener(object :
			OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				val width = relChosen.width
				VLog.d("Got width of view relChosen : $width")
				val layoutParams = relChosen.layoutParams as ConstraintLayout.LayoutParams
				layoutParams.width = width
				layoutParams.endToStart = UNSET
				layoutParams.startToStart = UNSET
				relChosen.viewTreeObserver.removeOnGlobalLayoutListener(this)
				relChosen.layoutParams = layoutParams
				binding.listeners(width)
			}
		})
	}


	private fun FragmentTibetswapBinding.listeners(width: Int) {

		txtSwap.setOnClickListener {
			if (vm.isShowingSwap)
				return@setOnClickListener
			vm.isShowingSwap = true
			animManager.moveViewToLeftByWidth(relChosen, width)
			txtSwap.setTextColor(requireActivity().getColorResource(R.color.green))
			txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.greey))

		}

		txtLiquidity.setOnClickListener {
			if (!vm.isShowingSwap)
				return@setOnClickListener
			vm.isShowingSwap = false
			animManager.moveViewToRightByWidth(relChosen, width)
			txtSwap.setTextColor(requireActivity().getColorResource(R.color.greey))
			txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.green))

		}

	}


}
