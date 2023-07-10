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
import com.example.common.tools.getLiquidityQuote
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTibetswapBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DynamicSpinnerAdapter
import com.green.wallet.presentation.custom.convertDpToPixel
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
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
import kotlinx.android.synthetic.main.fragment_tibetswap.edtAmountLiquidity
import kotlinx.android.synthetic.main.fragment_tibetswap.edtAmountTo
import kotlinx.android.synthetic.main.fragment_tibetswap.edtAmountXCH
import kotlinx.coroutines.Job
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

	val ad1Swap by lazy {
		TokenSpinnerAdapter(requireActivity(), listOf("XCH"))
	}

	val ad2Swap by lazy {
		TokenSpinnerAdapter(requireActivity(), listOf(""))
	}

	val adCATLiquidity by lazy {
		TokenSpinnerAdapter(requireActivity(), listOf(""))
	}

	val adTibetLiquidity by lazy {
		DynamicSpinnerAdapter(150, requireActivity(), listOf(""))
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = FragmentTibetswapBinding.inflate(layoutInflater)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		chooseWalletIfNeeded()
		with(binding) {
			commonListeners()
			prepareRelChosen()
			logicSwap()
			logicLiquidity()
			initDetailsTransSwap()
		}
		initTokenSwapAdapters()
		initTokenTibetAdapter()
		initCalculateOutput()
	}

	private fun FragmentTibetswapBinding.commonListeners() {
		btnGenerateOffer.setOnClickListener {
			if (vm.isShowingSwap) {
				getMainActivity().move2BtmCreateOfferXCHCATDialog()
			} else {
				getMainActivity().move2BtmCreateOfferLiquidityDialog()
			}
		}
	}

	private fun initTokenTibetAdapter() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.tokenTibetList.collectLatest {
					if (it.isNotEmpty()) {
						adTibetLiquidity.updateData(it.map { it.code })
					}
				}
			}
		}
	}


	private fun chooseWalletIfNeeded() {
		if (vm.curWallet != null) return
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
					if (it.isNotEmpty()) {
						ad2Swap.updateData(it.map { it.code })
						if (vm.xchToCAT) {
							binding.changeSwapAdapter(ad1Swap, ad2Swap)
							choosingXCHToCAT(true)
						} else {
							binding.changeSwapAdapter(ad2Swap, ad1Swap)
							choosingXCHToCAT(false)
						}
						adCATLiquidity.updateData(it.map { it.code })
						if (vm.catTibetAdapterPosition != -1)
							binding.tokenTibetCatSpinner.setSelection(vm.catTibetAdapterPosition)
						if (vm.catLiquidityAdapterPos != -1)
							binding.tokenTibetSpinner.setSelection(vm.catLiquidityAdapterPos)
					}
				}
			}
		}

		binding.imgSwap.setOnClickListener {
			resetTextAmountFrom()
			vm.xchToCAT = !vm.xchToCAT
			if (vm.xchToCAT) {
				binding.changeSwapAdapter(ad1Swap, ad2Swap)
				choosingXCHToCAT(true)
			} else {
				binding.changeSwapAdapter(ad2Swap, ad1Swap)
				choosingXCHToCAT(false)
			}
		}

	}

	private fun resetTextAmountFrom() {
		VLog.d("Resetting text amount from : ${binding.edtAmountFrom.text}")
		binding.edtAmountFrom.setText(binding.edtAmountFrom.text.toString())
	}

	private fun FragmentTibetswapBinding.prepareRelChosen() {
		relChosen.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
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
				imgArrowDownDetailTrans, requireActivity()
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
		if (this::animTransDetail.isInitialized && animTransDetail.isRunning) return
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
		} else animManager.rotateBy180Backward(binding.imgArrowDownDetailTrans, requireActivity())
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
			tokenToSpinner, imgArrowToSwap, requireActivity()
		)
		animManager.animateArrowIconCustomSpinner(
			tokenFromSpinner, imgArrowFromSwap, requireActivity()
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
		ad1: TokenSpinnerAdapter, ad2: TokenSpinnerAdapter
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
			tokenTibetSpinner, imgArrowTibet, requireActivity()
		)
		animManager.animateArrowIconCustomSpinner(
			tokenTibetCatSpinner, imgArrowCAT, requireActivity()
		)

		//cat Adapter
		tokenTibetCatSpinner.adapter = adCATLiquidity
		imgArrowCAT.setOnClickListener {
			tokenTibetCatSpinner.performClick()
		}

		//tibet Adapter
		tokenTibetSpinner.adapter = adTibetLiquidity
		imgArrowTibet.setOnClickListener {
			tokenTibetSpinner.performClick()
		}

		tokenTibetCatSpinner.onItemSelectedListener = object : OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				adCATLiquidity.selectedPosition = p2
				edtTokenCAT.text = adCATLiquidity.dataOptions[p2]
				vm.catTibetAdapterPosition = p2
				initTibetLiquidity()
				val tibetPos = getPositionOfCodeFromAdTibetLiquidity(adCATLiquidity.dataOptions[p2])
				if (tibetPos != -1) {
					tokenTibetSpinner.setSelection(tibetPos)
				}
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}

		tokenTibetSpinner.onItemSelectedListener = object : OnItemSelectedListener {
			override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
				adTibetLiquidity.selectedPosition = p2
				val tokenCode = adTibetLiquidity.dataOptions[p2].removeSuffix("-XCH")
				edtTokenCAT.text = tokenCode
				vm.catLiquidityAdapterPos = p2
				initTibetLiquidity()
				val tokenPos = getPositionOfCodeFromAdCATLiquidity(tokenCode)
				if (tokenPos != -1) {
					tokenTibetCatSpinner.setSelection(tokenPos)
				}
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {

			}

		}

		edtAmountCatTibet.addTextChangedListener {
			btnGenerateOffer.isEnabled = calculateTibetLiquidity(it.toString())
		}

		edtAmountLiquidity.addTextChangedListener {
			btnGenerateOffer.isEnabled = calculateTibetCATXCH(it.toString())
		}

	}

	private var tibetLiquidityJob: Job? = null
	private fun initTibetLiquidity() {
		tibetLiquidityJob?.cancel()
		tibetLiquidityJob = lifecycleScope.launch {
			val pairID = vm.tokenList.value[vm.catTibetAdapterPosition].pairID
			val res = vm.getTibetLiquidity(pairID)
			if (res != null) {
				vm.curTibetLiquidity = res
				binding.apply {
					if (vm.toTibet)
						edtAmountCatTibet.setText(edtAmountCatTibet.text.toString())
					else
						edtAmountLiquidity.setText(edtAmountLiquidity.text.toString())
				}
			}
		}
	}

	private fun calculateTibetLiquidity(str: String): Boolean {
		if (!vm.toTibet) return false
		val amount = str.toDoubleOrNull() ?: return false
		val tibetLiquid = vm.curTibetLiquidity ?: return false
		val liquidity = getLiquidityQuote(
			amount * 1000L,
			tibetLiquid.token_reserve,
			tibetLiquid.liquidity
		)
		binding.edtAmountLiquidity.setText((liquidity / 1000.0).toString())
		var xchDeposit = getLiquidityQuote(
			amount * 1000L,
			tibetLiquid.token_reserve,
			tibetLiquid.xch_reserve
		).toDouble()
		xchDeposit += liquidity
		xchDeposit /= PRECISION_XCH
		binding.edtAmountXCH.setText(formattedDoubleAmountWithPrecision(xchDeposit))
		vm.xchDeposit = xchDeposit
		vm.catTibetAmount = amount
		vm.liquidityAmount = liquidity / 1000.0
		return xchDeposit != 0.0 && liquidity != 0L
	}

	private fun calculateTibetCATXCH(str: String): Boolean {
		if (vm.toTibet) return false
		val amount = str.toDoubleOrNull() ?: return false
		val tibetLiquid = vm.curTibetLiquidity ?: return false
		val tokenAmount =
			((amount * 1000L).toInt() * tibetLiquid.token_reserve) / tibetLiquid.liquidity
		var xch = ((amount * 1000L).toLong() * tibetLiquid.xch_reserve) / tibetLiquid.liquidity
		xch += (amount * 1000L).toLong()
		binding.apply {
			edtAmountCatTibet.setText(formattedDoubleAmountWithPrecision(tokenAmount / 1000.0))
			edtAmountXCH.setText(formattedDoubleAmountWithPrecision(xch / PRECISION_XCH))
		}
		vm.xchDeposit = xch / PRECISION_XCH
		vm.liquidityAmount = amount
		vm.catTibetAmount = tokenAmount / 1000.0
		return xch != 0L && tokenAmount != 0L
	}

	private fun FragmentTibetswapBinding.changeLayoutPositions() {
		val layoutTibet = layoutExchangeTibet
		val layoutXCHCAT = layoutXCHCAT
		topContainer.removeAllViews()
		bottomContainer.removeAllViews()
		vm.toTibet = !vm.toTibet
		if (vm.toTibet) {
			topContainer.addView(layoutXCHCAT)
			bottomContainer.addView(layoutTibet)
			txtYouSendingXCH.text = requireActivity().getStringResource(R.string.you_send)
			txtYouSendingCAT.text = requireActivity().getStringResource(R.string.you_send)
			txtYouSendingTibet.text = requireActivity().getStringResource(R.string.you_receive)
			imgArrowTibet.visibility = View.GONE
			imgArrowCAT.visibility = View.VISIBLE
			edtTokenCAT.setTextColor(requireActivity().getColorResource(R.color.secondary_text_color))
			edtTokenTibet.setTextColor(requireActivity().getColorResource(R.color.hint_color))
			edtAmountLiquidity.isEnabled = false
			edtAmountCatTibet.isEnabled = true
		} else {
			topContainer.addView(layoutTibet)
			bottomContainer.addView(layoutXCHCAT)
			txtYouSendingXCH.text = requireActivity().getStringResource(R.string.you_receive)
			txtYouSendingCAT.text = requireActivity().getStringResource(R.string.you_receive)
			txtYouSendingTibet.text = requireActivity().getStringResource(R.string.you_send)
			imgArrowTibet.visibility = View.VISIBLE
			imgArrowCAT.visibility = View.GONE
			edtTokenCAT.setTextColor(requireActivity().getColorResource(R.color.hint_color))
			edtTokenTibet.setTextColor(requireActivity().getColorResource(R.color.secondary_text_color))
			edtAmountLiquidity.isEnabled = true
			edtAmountCatTibet.isEnabled = false
		}
	}

	private fun FragmentTibetswapBinding.listenersForSwapDest(width: Int) {

		txtSwap.setOnClickListener {
			if (vm.isShowingSwap) return@setOnClickListener
			val prevRunning = animManager.moveViewToLeftByWidth(relChosen, width)
			if (prevRunning) return@setOnClickListener
			vm.isShowingSwap = true
			txtSwap.setTextColor(requireActivity().getColorResource(R.color.green))
			txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.greey))
			scrollLiquidity.visibility = View.GONE
			scrollSwap.visibility = View.VISIBLE
		}

		txtLiquidity.setOnClickListener {
			if (!vm.isShowingSwap) return@setOnClickListener
			val prevRunning = animManager.moveViewToRightByWidth(relChosen, width)
			if (prevRunning) return@setOnClickListener
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

	private fun getPositionOfCodeFromAdCATLiquidity(code: String): Int {
		for (i in 0 until adCATLiquidity.dataOptions.size) {
			if (adCATLiquidity.dataOptions[i] == code)
				return i
		}
		return -1
	}

	private fun getPositionOfCodeFromAdTibetLiquidity(code: String): Int {
		for (i in 0 until adTibetLiquidity.dataOptions.size) {
			if (adTibetLiquidity.dataOptions[i].contains(code))
				return i
		}
		return -1
	}

	override fun onDestroyView() {
		super.onDestroyView()
		VLog.d("On Destroy view for Tibet swap fragment")
		vm.swapMainScope?.cancel()
	}


}
