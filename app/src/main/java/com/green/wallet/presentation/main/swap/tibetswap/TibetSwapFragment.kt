package com.green.wallet.presentation.main.swap.tibetswap

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.InputFilter
import android.text.Spanned
import android.text.method.LinkMovementMethod
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
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.DynamicSpinnerAdapter
import com.green.wallet.presentation.custom.base.BaseFragment
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
import kotlinx.android.synthetic.main.fragment_tibetswap.containerSwap
import kotlinx.android.synthetic.main.fragment_tibetswap.edtAmountTo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

class TibetSwapFragment : BaseFragment(), BtmCreateOfferXCHCATDialog.OnXCHCATListener {

    private lateinit var binding: FragmentTibetswapBinding

    @Inject
    lateinit var animManager: AnimationManager

    @Inject
    lateinit var dialogManager: DialogManager

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
        DynamicSpinnerAdapter(150, requireActivity(), mutableListOf(""))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTibetswapBinding.inflate(layoutInflater)
        VLog.d("On Create View on tibet swap fragment")
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VLog.d("On View Created on tibet swap fragment")
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
        initSuccessClearingFields()
        vm.initWalletList()
        vm.retrieveTibetTokenList()
        vm.retrieveTokenList()
    }

    override suspend fun collectingFlowsOnStarted() {
        vm.fiveMinTillGetListOfTokensFromTibet()
    }

    private fun initSuccessClearingFields() {
        vm.onSuccessTibetSwapClearingFields = {
            binding.apply {
                edtAmountFrom.setText("")
                edtAmountTo.text = ""
                vm.swapInputState = ""
                vm.tibetSwapReInitToNullValue()
            }
        }
        vm.onSuccessTibetLiquidityClearingFields = {
            binding.apply {
                edtAmountCatTibet.setText("")
                edtAmountLiquidity.setText("")
                edtAmountXCH.setText("")
            }
        }
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
                    it?.let {
                        if (it.isNotEmpty()) {
                            adTibetLiquidity.updateData(it.map { it.code })
                        } else {
                            requireActivity().apply {
                                dialogManager.showFailureDialog(
                                    this,
                                    getStringResource(R.string.service_is_unavailable),
                                    getStringResource(R.string.failed),
                                    getStringResource(R.string.ok_button)
                                ) {

                                }
                            }
                            binding.btnGenerateOffer.isEnabled = false
                        }
                    }
                }
            }
        }
    }


    private fun chooseWalletIfNeeded() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.walletList.collectLatest {
                    it?.let {
                        if (it.isNotEmpty()) {
                            if (it.size == 1) {
                                vm.curWallet = it[0]
                            } else {
                                getMainActivity().move2BtmChooseWalletDialog()
                            }
                        } else {
                            getMainActivity().apply {
                                dialogManager.showFailureDialog(
                                    this,
                                    status = getStringResource(R.string.pop_up_failed_create_a_mnemonic_phrase_title),
                                    description = getStringResource(R.string.exchange_fail),
                                    action = getStringResource(R.string.network_description_btn),
                                    false
                                ) {
                                    getMainActivity().showBtmDialogCreateOrImportNewWallet(false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initCalculateOutput() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.tibetSwap.collectLatest {
                    it?.let {
                        when (it.state) {
                            Resource.State.SUCCESS -> {
                                val amountOut = (it.data?.amount_out
                                    ?: 0) / if (vm.xchToCAT) PRECISION_CAT else PRECISION_XCH
                                val priceWarning = it.data?.price_warning ?: false
                                edtAmountTo.text = formattedDollarWithPrecision(amountOut, 12)
                                binding.apply {
                                    calculateCoursePrice(amountOut)
                                    linearAgree.visibility =
                                        if (priceWarning) View.VISIBLE else View.GONE
                                    edtUpdateCourse.setText(
                                        "${
                                            formattedDollarWithPrecision(
                                                (it.data?.price_impact ?: 0.0) * 100,
                                                2
                                            )
                                        }%"
                                    )
                                    if (priceWarning) {
                                        checkboxAgree.isChecked = false
                                        btnGenerateOffer.isEnabled = false
                                    }
                                }
                                vm.isPriceImpacted = priceWarning
                                initNoAnimationCollapsingDetailTransaction(containerSwap)
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

    private fun FragmentTibetswapBinding.calculateCoursePrice(amountOut: Double) {
        val amountIn = edtAmountFrom.text.toString().toDoubleOrNull() ?: 0.0
        val tokenCode = vm.tokenList.value?.get(vm.catAdapPosition)?.code
        if (amountIn == 0.0) {
            txtCoursePrice.setText("1 $tokenCode = ∞ XCH")
            return
        }
        val xchAmount = if (vm.xchToCAT) amountIn else amountOut
        val catAmount = if (vm.xchToCAT) amountOut else amountIn
        val oneToken = xchAmount / catAmount
        val format = formattedDoubleAmountWithPrecision(oneToken, 9)
        if (vm.tokenList.value?.isEmpty() == true) return
        txtCoursePrice.setText("1 $tokenCode = $format XCH")
    }

    private fun initTokenSwapAdapters() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.tokenList.collectLatest {
                    it?.let {
                        if (it.isNotEmpty()) {
                            VLog.d("On collection based on code : ${it.map { it.code }}")
                            ad2Swap.updateData(it.map { it.code })
                            if (vm.xchToCAT) {
                                binding.changeSwapAdapter(ad1Swap, ad2Swap)
                                choosingXCHToCAT(true)
                            } else {
                                binding.changeSwapAdapter(ad2Swap, ad1Swap)
                                choosingXCHToCAT(false)
                            }
                            adCATLiquidity.updateData(it.map { it.code })
                            if (vm.catTibetAdapterPosition != -1) binding.tokenTibetCatSpinner.setSelection(
                                vm.catTibetAdapterPosition
                            )
                            if (vm.catLiquidityAdapterPos != -1) binding.tokenTibetSpinner.setSelection(
                                vm.catLiquidityAdapterPos
                            )
                        } else {
                            requireActivity().apply {
                                dialogManager.showFailureDialog(
                                    this,
                                    getStringResource(R.string.service_is_unavailable),
                                    getStringResource(R.string.failed),
                                    getStringResource(R.string.ok_button)
                                ) {

                                }
                            }
                            binding.btnGenerateOffer.isEnabled = false
                        }
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
        if (binding.edtAmountFrom.text.toString().isEmpty())
            return
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
            params.height = requireActivity().convertDpToPixel(vm.getContainerBiggerSize())
            containerSwap.layoutParams = params
        } else {
            val params = containerSwap.layoutParams
            params.height = requireActivity().convertDpToPixel(vm.getContainerSmallerSize())
            containerSwap.layoutParams = params
        }
        imgArrowDownDetailTrans.setOnClickListener {
            initAnimationCollapsingDetailTransaction(containerSwap)
        }
    }

    lateinit var animTransDetail: ValueAnimator
    fun initAnimationCollapsingDetailTransaction(layout: View) {
        if (this::animTransDetail.isInitialized && animTransDetail.isRunning) return
        val newHeightPixel = (vm.getNextHeightSize() * resources.displayMetrics.density).toInt()
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

    }

    fun initNoAnimationCollapsingDetailTransaction(layout: View) {
        if (this::animTransDetail.isInitialized && animTransDetail.isRunning) return
        val curHeight =
            if (vm.nextContainerBigger) vm.getContainerSmallerSize() else vm.getContainerBiggerSize()
        val newHeightPixel = (curHeight * resources.displayMetrics.density).toInt()
        animTransDetail = ValueAnimator.ofInt(layout.height, newHeightPixel)
        animTransDetail.duration = 1 // duration in milliseconds
        animTransDetail.interpolator = DecelerateInterpolator()
        animTransDetail.addUpdateListener {
            val value = it.animatedValue as Int
            layout.layoutParams.height = value
            layout.requestLayout()
        }
        animTransDetail.start()
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

        if (vm.swapInputState.isNotEmpty())
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
        constraintAfterCommaXCHCAT(xchToCat)
    }

    private fun constraintAfterCommaXCHCAT(xchToCat: Boolean) {
        if (!xchToCat) {
            binding.apply {
                if (edtAmountFrom.text.toString().isEmpty())
                    return
                val formatted = formattedDollarWithPrecision(
                    edtAmountFrom.text.toString().toDoubleOrNull() ?: 0.0, 3
                )
                edtAmountFrom.setText(formatted)
            }
        }
        val constraint = if (xchToCat) 12 else 3
        val filter = object : InputFilter {
            override fun filter(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Spanned?,
                p4: Int,
                p5: Int
            ): CharSequence {
                if (p0 == null) return ""
                val curText = binding.edtAmountFrom.text.toString()
                val locComo = curText.indexOf('.')
                val cursor = binding.edtAmountFrom.selectionStart
                if (locComo == -1 || locComo == 0 || cursor <= locComo)
                    return p0
                val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
                if (digitsAfterComo >= constraint) {
                    return ""
                }
                return p0
            }
        }
        binding.edtAmountFrom.filters = arrayOf(filter)
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

        initConstraintsLiquidityEnterAmount()

    }

    private fun initConstraintsLiquidityEnterAmount() {

        val catFilter = object : InputFilter {
            override fun filter(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Spanned?,
                p4: Int,
                p5: Int
            ): CharSequence {
                if (p0 == null) return ""
                val curText = binding.edtAmountCatTibet.text.toString()
                val locComo = curText.indexOf('.')
                val cursor = binding.edtAmountCatTibet.selectionStart
                if (locComo == -1 || locComo == 0 || cursor <= locComo)
                    return p0
                val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
                if (digitsAfterComo >= 3) {
                    return ""
                }
                return p0
            }
        }

        binding.edtAmountCatTibet.filters = arrayOf(catFilter)
        val tibetFilter = object : InputFilter {
            override fun filter(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Spanned?,
                p4: Int,
                p5: Int
            ): CharSequence {
                if (p0 == null) return ""
                val curText = binding.edtAmountLiquidity.text.toString()
                val locComo = curText.indexOf('.')
                val cursor = binding.edtAmountLiquidity.selectionStart
                if (locComo == -1 || locComo == 0 || cursor <= locComo)
                    return p0
                val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
                if (digitsAfterComo >= 3) {
                    return ""
                }
                return p0
            }
        }
        binding.edtAmountLiquidity.filters = arrayOf(tibetFilter)

    }


    private var tibetLiquidityJob: Job? = null
    private fun initTibetLiquidity() {
        tibetLiquidityJob?.cancel()
        tibetLiquidityJob = lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->

        }) {
            val pairID =
                vm.tokenTibetList.value?.get(vm.catTibetAdapterPosition)?.pairID ?: return@launch
            val res = vm.getTibetLiquidity(pairID)
            if (res != null) {
                vm.curTibetLiquidity = res
                binding.apply {
                    if (vm.toTibet) edtAmountCatTibet.setText(edtAmountCatTibet.text.toString())
                    else edtAmountLiquidity.setText(edtAmountLiquidity.text.toString())
                }
            }
        }
    }

    private fun calculateTibetLiquidity(str: String): Boolean {
        if (!vm.toTibet) return false
        val amount = str.toDoubleOrNull() ?: return false
        val tibetLiquid = vm.curTibetLiquidity ?: return false
        val liquidity = getLiquidityQuote(
            amount * 1000L, tibetLiquid.token_reserve, tibetLiquid.liquidity
        )
        binding.edtAmountLiquidity.setText((liquidity / 1000.0).toString())
        var xchDeposit = getLiquidityQuote(
            amount * 1000L, tibetLiquid.token_reserve, tibetLiquid.xch_reserve
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
        val xch =
            ((BigInteger("${(1000 * amount).toLong()}").multiply(BigInteger("${tibetLiquid.xch_reserve}"))).divide(
                BigInteger("${tibetLiquid.liquidity}")
            )).plus(BigInteger("${(amount * 1000).toLong()}")).toDouble()
        binding.apply {
            edtAmountCatTibet.setText(formattedDoubleAmountWithPrecision(tokenAmount / 1000.0))
            edtAmountXCH.setText(formattedDoubleAmountWithPrecision(xch / PRECISION_XCH))
        }
        vm.xchDeposit = xch / PRECISION_XCH
        vm.liquidityAmount = amount
        vm.catTibetAmount = tokenAmount / 1000.0
        return xch != 0.0 && tokenAmount != 0L
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
            txtLiquidityState.apply {
                text = requireActivity().getStringResource(R.string.add_liquidity)
                setTextColor(requireActivity().getColorResource(R.color.green))
            }
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
            txtLiquidityState.apply {
                text = requireActivity().getStringResource(R.string.remove_liquidity)
                setTextColor(requireActivity().getColorResource(R.color.red_mnemonic))
            }
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
            requireActivity().apply {
                dialogManager.showWarningPriceChangeDialog(
                    this,
                    getStringResource(R.string.risk_warning),
                    getStringResource(R.string.risk_warning_text),
                    getStringResource(R.string.ok_button)
                ) {

                }
            }
        }

        if (!vm.isShowingSwap) {
            animManager.moveViewToRightByWidthNoAnim(relChosen, width)
            txtSwap.setTextColor(requireActivity().getColorResource(R.color.greey))
            txtLiquidity.setTextColor(requireActivity().getColorResource(R.color.green))
            scrollLiquidity.visibility = View.VISIBLE
            scrollSwap.visibility = View.GONE
        }

        binding.apply {
            checkboxText.text =
                Html.fromHtml(requireActivity().getStringResource(R.string.price_impact))
            checkboxText.setMovementMethod(LinkMovementMethod.getInstance())
        }

        checkboxAgree.setOnCheckedChangeListener { compoundButton, checked ->
            btnGenerateOffer.isEnabled = checked
        }

    }

    private fun getPositionOfCodeFromAdCATLiquidity(code: String): Int {
        for (i in 0 until adCATLiquidity.dataOptions.size) {
            if (adCATLiquidity.dataOptions[i] == code) return i
        }
        return -1
    }

    private fun getPositionOfCodeFromAdTibetLiquidity(code: String): Int {
        for (i in 0 until adTibetLiquidity.dataOptions.size) {
            if (adTibetLiquidity.dataOptions[i].contains(code)) return i
        }
        return -1
    }

    override fun onStart() {
        super.onStart()
        VLog.d("On Start on tibet swap fragment")
    }

    override fun onResume() {
        super.onResume()
        VLog.d("On Resume on tibet swap fragment")
    }

    override fun onPause() {
        super.onPause()
        VLog.d("On Pause on tibet swap fragment")
    }

    override fun onStop() {
        super.onStop()
        VLog.d("On Stop on tibet swap fragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        VLog.d("On Destroy view for Tibet swap fragment")
        vm.swapMainScope?.cancel()
    }

    override fun onSuccessClearFields() {

    }


}
