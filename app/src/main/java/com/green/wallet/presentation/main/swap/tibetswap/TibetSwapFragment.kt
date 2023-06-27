package com.green.wallet.presentation.main.swap.tibetswap

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTibetswapBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.convertDpToPixel
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.TokenSpinnerAdapter
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.tools.makeGreenDuringFocus
import com.green.wallet.presentation.tools.makeGreyDuringNonFocus
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_tibetswap.edtAmountTo
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetSwapFragment : DaggerFragment() {

	private lateinit var binding: FragmentTibetswapBinding

	@Inject
	lateinit var animManager: AnimationManager

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetSwapViewModel by viewModels { viewModelFactory }

	val ad1 by lazy {
		TokenSpinnerAdapter(requireActivity(), listOf("XCH"))
	}

	val ad2 by lazy {
		TokenSpinnerAdapter(requireActivity(), listOf(""))
	}

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
		chooseWalletIfNeeded()
		with(binding) {
			prepareRelChosen()
			logicSwap()
			logicLiquidity()
			initDetailsTransSwap()
		}
		initTokenSwapAdapters()
		initCalculateOutput()
	}

	private fun chooseWalletIfNeeded() {
		if (vm.curWallet != null)
			return
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.walletList.collectLatest {
					if (it.isNotEmpty()) {
						if (it.size == 1) {
							vm.curWallet = it[0]
						} else {
							getMainActivity().move2BtmChooseWalletDialog()
						}
					}
				}
			}
		}
	}

	private fun initCalculateOutput() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.tibetSwap.collectLatest {
					it?.let {
						when (it.state) {
							Resource.State.SUCCESS -> {
								val amountOut =
									it.data!!.amount_out / if (vm.xchToCAT) PRECISION_CAT else PRECISION_XCH
								edtAmountTo.text = formattedDollarWithPrecision(amountOut, 12)
							}

							Resource.State.ERROR -> {

							}

							Resource.State.LOADING -> {

							}
						}
					}
				}
			}
		}
	}

	private fun initTokenSwapAdapters() {

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.tokenList.collectLatest {
					ad2.updateData(it.map { it.code })
					if (vm.xchToCAT) {
						binding.changeSwapAdapter(ad1, ad2)
						choosingXCHToCAT(true)
					} else {
						binding.changeSwapAdapter(ad2, ad1)
						choosingXCHToCAT(false)
					}
				}
			}
		}

		binding.imgSwap.setOnClickListener {
			resetTextAmountFrom()
			vm.xchToCAT = !vm.xchToCAT
			if (vm.xchToCAT) {
				binding.changeSwapAdapter(ad1, ad2)
				choosingXCHToCAT(true)
			} else {
				binding.changeSwapAdapter(ad2, ad1)
				choosingXCHToCAT(false)
			}
		}

	}

	private fun resetTextAmountFrom() {
		VLog.d("Resetting text amount from : ${binding.edtAmountFrom.text}")
		binding.edtAmountFrom.setText(binding.edtAmountFrom.text.toString())
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
				binding.listenersForSwapDest(width)
			}
		})
	}


	private fun FragmentTibetswapBinding.initDetailsTransSwap() {
		//detail tran
		if (!vm.nextContainerBigger) {
			animManager.rotateBy180ForwardNoAnimation(
				imgArrowDownDetailTrans,
				requireActivity()
			)
			val params = containerSwap.layoutParams
			params.height = requireActivity().convertDpToPixel(vm.containerBiggerSize)
			containerSwap.layoutParams = params
		} else {
			val params = containerSwap.layoutParams
			params.height = requireActivity().convertDpToPixel(vm.containerSmallerSize)
			containerSwap.layoutParams = params
		}
		imgArrowDownDetailTrans.setOnClickListener {
			initAnimationCollapsingDetailTransaction(containerSwap)
		}
	}

	lateinit var animTransDetail: ValueAnimator
	fun initAnimationCollapsingDetailTransaction(layout: View) {
		if (this::animTransDetail.isInitialized && animTransDetail.isRunning)
			return
		val newHeightPixel = (vm.nextHeight * resources.displayMetrics.density).toInt()
		animTransDetail = ValueAnimator.ofInt(layout.height, newHeightPixel)
		animTransDetail.duration = 500 // duration in milliseconds
		animTransDetail.interpolator = DecelerateInterpolator()
		animTransDetail.addUpdateListener {
			val value = it.animatedValue as Int
			layout.layoutParams.height = value
			layout.requestLayout()
		}
		animTransDetail.start()
		if (vm.nextContainerBigger) {
			animManager.rotateBy180Forward(binding.imgArrowDownDetailTrans, requireActivity())
		} else
			animManager.rotateBy180Backward(binding.imgArrowDownDetailTrans, requireActivity())
		vm.nextContainerBigger = !vm.nextContainerBigger
		if (vm.nextContainerBigger) {
			vm.nextHeight = vm.containerBiggerSize
		} else {
			vm.nextHeight = vm.containerSmallerSize
		}
	}

	private fun FragmentTibetswapBinding.logicSwap() {

		vm.startDebounceValueSwap()

		imgArrowToSwap.setOnClickListener {
			tokenToSpinner.performClick()
		}

		imgArrowFromSwap.setOnClickListener {
			tokenFromSpinner.performClick()
		}

		animManager.animateArrowIconCustomSpinner(
			tokenToSpinner,
			imgArrowToSwap,
			requireActivity()
		)
		animManager.animateArrowIconCustomSpinner(
			tokenFromSpinner,
			imgArrowFromSwap,
			requireActivity()
		)


		edtAmountFrom.addTextChangedListener {
			VLog.d("Text Changed On edtAmountFrom : $it")
			val amount = it.toString().toDoubleOrNull() ?: 0.0
			if (amount != 0.0) {
				vm.onInputSwapAmountChanged(amount)
			}
			vm.swapInputState = it.toString()
			btnGenerateOffer.isEnabled = amount != 0.0
		}

		edtAmountFrom.setOnFocusChangeListener { p0, p1 ->
			if (p1) {
				getMainActivity().makeGreenDuringFocus(txtYouSending)
				greenLineEdt.visibility = View.VISIBLE
			} else {
				getMainActivity().makeGreyDuringNonFocus(txtYouSending)
				greenLineEdt.visibility = View.GONE
			}
		}


		btnGenerateOffer.setOnClickListener {
			if (vm.isShowingSwap) {
				getMainActivity().move2BtmCreateOfferDialog()
			}
		}

		edtAmountFrom.setText(vm.swapInputState)

	}

	private fun choosingXCHToCAT(xchToCat: Boolean) {
		binding.apply {
			imgArrowFromSwap.clearAnimation()
			imgArrowToSwap.clearAnimation()
			if (xchToCat) {
				imgArrowFromSwap.visibility = View.GONE
				imgArrowToSwap.visibility = View.VISIBLE
				edtFromNetwork.text = "Chia Network"
				edtToNetwork.text = "CAT2"
			} else {
				imgArrowFromSwap.visibility = View.VISIBLE
				imgArrowToSwap.visibility = View.GONE
				edtFromNetwork.text = "CAT2"
				edtToNetwork.text = "Chia Network"
			}
		}
	}

	private fun FragmentTibetswapBinding.changeSwapAdapter(
		ad1: TokenSpinnerAdapter,
		ad2: TokenSpinnerAdapter
	) {

		tokenFromSpinner.adapter = ad1
		tokenToSpinner.adapter = ad2
		tokenFromSpinner.onItemSelectedListener = object : OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				val opt = ad1.dataOptions[p2]
				edtTokenFrom.text = opt
				ad1.selectedPosition = p2
				if (!vm.xchToCAT) {
					vm.catAdapPosition = p2
				}
				resetTextAmountFrom()
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}

		tokenToSpinner.onItemSelectedListener = object : OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				val opt = ad2.dataOptions[p2]
				edtTokenTo.text = opt
				ad2.selectedPosition = p2
				if (vm.xchToCAT) {
					vm.catAdapPosition = p2
				}
				resetTextAmountFrom()
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}

		tokenFromSpinner.setSelection(if (vm.xchToCAT) 0 else vm.catAdapPosition)
		tokenToSpinner.setSelection(if (vm.xchToCAT) vm.catAdapPosition else 0)

	}

	private fun FragmentTibetswapBinding.logicLiquidity() {

		//swapBtnTibet
		imgSwapLiquid.setOnClickListener {
			changeLayoutPositions()
		}

		animManager.animateArrowIconCustomSpinner(
			tokenTibetSpinner,
			imgArrowTibet,
			requireActivity()
		)
		animManager.animateArrowIconCustomSpinner(
			tokenTibetCatSpinner,
			imgArrowCAT,
			requireActivity()
		)
		//cat Adapter
		val catAdapter = TokenSpinnerAdapter(requireActivity(), listOf("NION", "USDT", "GWT"))
		tokenTibetCatSpinner.adapter = catAdapter
		imgArrowCAT.setOnClickListener {
			tokenTibetCatSpinner.performClick()
		}

		//tibet Adapter
		val tibetAdapter = TokenSpinnerAdapter(requireActivity(), listOf("NION", "USDT"))
		tokenTibetSpinner.adapter = tibetAdapter
		imgArrowTibet.setOnClickListener {
			tokenTibetSpinner.performClick()
		}

		tokenTibetCatSpinner.onItemSelectedListener = object : OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				catAdapter.selectedPosition = p2
				edtTokenCAT.text = catAdapter.dataOptions[p2]
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}

		tokenTibetSpinner.onItemSelectedListener = object : OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				tibetAdapter.selectedPosition = p2
				edtTokenTibet.text = tibetAdapter.dataOptions[p2]
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}


	}

	private fun FragmentTibetswapBinding.changeLayoutPositions() {
		val layoutTibet = layoutExchangeTibet
		val layoutXCHCAT = layoutXCHCAT
		topContainer.removeAllViews()
		bottomContainer.removeAllViews()
		if (vm.toTibet) {
			topContainer.addView(layoutXCHCAT)
			bottomContainer.addView(layoutTibet)
			txtYouSendingXCH.text = requireActivity().getStringResource(R.string.you_send)
			txtYouSendingCAT.text = requireActivity().getStringResource(R.string.you_send)
			txtYouSendingTibet.text = requireActivity().getStringResource(R.string.you_receive)
		} else {
			topContainer.addView(layoutTibet)
			bottomContainer.addView(layoutXCHCAT)
			txtYouSendingXCH.text = requireActivity().getStringResource(R.string.you_receive)
			txtYouSendingCAT.text = requireActivity().getStringResource(R.string.you_receive)
			txtYouSendingTibet.text = requireActivity().getStringResource(R.string.you_send)
		}
		vm.toTibet = !vm.toTibet
	}

	private fun FragmentTibetswapBinding.listenersForSwapDest(width: Int) {

		txtSwap.setOnClickListener {
			if (vm.isShowingSwap)
				return@setOnClickListener
			val prevRunning = animManager.moveViewToLeftByWidth(relChosen, width)
			if (prevRunning)
				return@setOnClickListener
			vm.isShowingSwap = true
			txtSwap.setTextColor(requireActivity().getColorResource(R.color.green))
			txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.greey))
			scrollLiquidity.visibility = View.GONE
			scrollSwap.visibility = View.VISIBLE
		}

		txtLiquidity.setOnClickListener {
			if (!vm.isShowingSwap)
				return@setOnClickListener
			val prevRunning = animManager.moveViewToRightByWidth(relChosen, width)
			if (prevRunning)
				return@setOnClickListener
			vm.isShowingSwap = false
			txtSwap.setTextColor(requireActivity().getColorResource(R.color.greey))
			txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.green))
			scrollLiquidity.visibility = View.VISIBLE
			scrollSwap.visibility = View.GONE
		}

		if (!vm.isShowingSwap) {
			animManager.moveViewToRightByWidthNoAnim(relChosen, width)
			txtSwap.setTextColor(requireActivity().getColorResource(R.color.greey))
			txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.green))
			scrollLiquidity.visibility = View.VISIBLE
			scrollSwap.visibility = View.GONE
		}
	}


	override fun onDestroyView() {
		super.onDestroyView()
		VLog.d("On Destroy view for Tibet swap fragment")
		vm.swapMainScope?.cancel()
	}


}
