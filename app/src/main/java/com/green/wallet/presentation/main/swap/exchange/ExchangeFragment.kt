package com.green.wallet.presentation.main.swap.exchange

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.tools.formatString
import com.green.wallet.R
import com.green.wallet.databinding.FragmentExchangeBinding
import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.CustomSpinner
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.DynamicSpinnerAdapter
import com.green.wallet.presentation.custom.convertDpToPixel
import com.green.wallet.presentation.custom.hidePublicKey
import com.green.wallet.presentation.custom.manageExceptionDialogsForRest
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.TokenSpinnerAdapter
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.tools.makeGreenDuringFocus
import com.green.wallet.presentation.tools.makeGreyDuringNonFocus
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_exchange.tokenToSpinner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExchangeFragment : DaggerFragment() {

	private lateinit var binding: FragmentExchangeBinding

	@Inject
	lateinit var animManager: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: ExchangeViewModel by viewModels { viewModelFactory }

	override fun onAttach(context: Context) {
		super.onAttach(context)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		VLog.d("On Create on exchange fragment")
	}

	private var smallContainer = 300
	private var bigContainer = 418
	private var hasOneFocusLeast = false

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentExchangeBinding.inflate(layoutInflater)
		VLog.d("On Create view on exchange fragment")
		binding.registerViews()
		binding.registerFilters()
		binding.initSpinners()
		return binding.root
	}

	private fun initChiaWalletAdapter() {
		lifecycleScope.launchWhenCreated {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.chiaWalletList.collectLatest {
					val list = it.map { "Chia ${it.fingerPrint}" }
					val adapter = DynamicSpinnerAdapter(170, getMainActivity(), list)
					binding.walletSpinner.apply {
						this.adapter = adapter
						onItemSelectedListener = object : OnItemSelectedListener {
							override fun onItemSelected(
								p0: AdapterView<*>?,
								p1: View?,
								p2: Int,
								p3: Long
							) {
								vm.walletPosition = p2
								adapter.selectedPosition = p2
								binding.apply {
									edtFingerPrint.text = hidePublicKey(it[p2].fingerPrint)
									edtGetAddress.text = formatString(10, it[p2].address, 6)
								}
							}

							override fun onNothingSelected(p0: AdapterView<*>?) {

							}

						}
						setSelection(vm.walletPosition)
					}
				}
			}
		}
	}

	private fun FragmentExchangeBinding.registerViews() {

		initDetailTransaction(container, txtDetailTransactions, imgArrowDownDetailTrans)
		animManager.animateArrowIconCustomSpinner(
			binding.tokenToSpinner,
			imgArrowTo,
			getMainActivity()
		)
		animManager.animateArrowIconCustomSpinner(
			binding.tokenFromSpinner,
			imgArrowFrom,
			getMainActivity()
		)

		edtAmountFrom.setOnFocusChangeListener { p0, p1 ->
			if (p1 && !hasOneFocusLeast) {
				hasOneFocusLeast = true
				smallContainer = 400
				bigContainer = 520
				nextHeight = bigContainer
				viewDividerGetAddress.visibility = View.VISIBLE
				initGetAddressLayoutUpdate()
				val newHeightPixel = (smallContainer * resources.displayMetrics.density).toInt()
				anim = ValueAnimator.ofInt(container.height, newHeightPixel)
				anim.duration = 500 // duration in milliseconds
				anim.interpolator = DecelerateInterpolator()
				anim.addUpdateListener {
					val value = it.animatedValue as Int
					container.layoutParams.height = value
					container.requestLayout()
				}
				anim.start()
			}
			if (p1) {
				getMainActivity().makeGreenDuringFocus(txtYouSending)
				greenLineEdt.visibility = View.VISIBLE
			} else {
				getMainActivity().makeGreyDuringNonFocus(txtYouSending)
				greenLineEdt.visibility = View.GONE
			}
		}

		icQuestion.setOnClickListener {
			getMainActivity().apply {
				dialogManager.showQuestionDialogExchange(
					this,
					"Фиксированный курс",
					"Сумма к получению останется неизменной независимо от изменений на рынке.\n" +
							"\n" +
							"Фиксированный курс обновляется каждые 30 сек.",
					getStringResource(R.string.ok_button)
				) {

				}
			}
		}

		txtMinSumRequired.text =
			"${getMainActivity().getStringResource(R.string.min_sum)}: 36.70 USDT TRC-20"
		val second = getMainActivity().getStringResource(R.string.sec_rate_update)
		val threeLetter = second.substring(0, Math.min(second.length, 3))
		edtUpdateCourse.text = "3 $threeLetter"

		tokenFromSpinner.setSpinnerEventsListener(object : CustomSpinner.OnSpinnerEventsListener {
			override fun onSpinnerOpened(spin: Spinner?) {
				edtFromNetwork.setTextColor(getMainActivity().getColorResource(R.color.green))
			}

			override fun onSpinnerClosed(spin: Spinner?) {
				edtFromNetwork.setTextColor(getMainActivity().getColorResource(R.color.grey_txt_color))
			}

		})

		tokenToSpinner.setSpinnerEventsListener(object : CustomSpinner.OnSpinnerEventsListener {
			override fun onSpinnerOpened(spin: Spinner?) {
				edtToNetwork.setTextColor(getMainActivity().getColorResource(R.color.green))
			}

			override fun onSpinnerClosed(spin: Spinner?) {
				edtToNetwork.setTextColor(getMainActivity().getColorResource(R.color.grey_txt_color))
			}

		})

		edtGetAddressUSDT.setOnFocusChangeListener { view, focus ->
			if (focus) {
				imgIcScanUsdt.visibility = View.GONE
				lineGreenEdtUsdt.visibility = View.VISIBLE
				getMainActivity().makeGreenDuringFocus(txtReceiveAddressUSDT)
			} else if (edtGetAddressUSDT.text.toString().isEmpty()) {
				lineGreenEdtUsdt.visibility = View.GONE
				imgIcScanUsdt.visibility = View.VISIBLE
			}
			if (!focus) {
				lineGreenEdtUsdt.visibility = View.GONE
				getMainActivity().makeGreyDuringNonFocus(txtReceiveAddressUSDT)
			}
		}

		imgIcScanUsdt.setOnClickListener {
			requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
		}

		imgArrowChia.setOnClickListener {
			walletSpinner.performClick()
		}

		animManager.animateArrowIconCustomSpinner(
			binding.walletSpinner,
			binding.imgArrowChia,
			getMainActivity()
		)

		imgIcScanUsdt.setOnClickListener {
			requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
		}

		edtGetAddressUSDT.addTextChangedListener {
			if (it?.isNotEmpty() == true) {
				imgIcScanUsdt.visibility = View.VISIBLE
			}
		}

	}


	private val requestPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it ->
			it.entries.forEach {
				if (it.value) {
					getMainActivity().mainViewModel.saveDecodeQrCode("")
					getMainActivity().move2ScannerFragment(null, null)
				} else
					Toast.makeText(
						getMainActivity(),
						getMainActivity().getStringResource(R.string.camera_permission_missing),
						Toast.LENGTH_SHORT
					)
						.show()
			}
		}

	private var prevEnterAddressJob: Job? = null
	private fun getAddressToSend() {
		prevEnterAddressJob?.cancel()
		prevEnterAddressJob = lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				launch {
					getMainActivity().mainViewModel.decodedQrCode.collectLatest {
						if (it.isNotEmpty()) {
							binding.edtGetAddressUSDT.setText(it)
							getMainActivity().mainViewModel.saveDecodeQrCode("")
						}
					}
				}
			}
		}
	}


	private fun FragmentExchangeBinding.initSpinners() {
		val tokenFromAdapter = TokenSpinnerAdapter(getMainActivity(), listOf("XCH", "USDT"))
		val tokenToAdapter = TokenSpinnerAdapter(getMainActivity(), listOf("XCH", "USDT"))
		tokenToSpinner.adapter = tokenToAdapter
		tokenFromSpinner.adapter = tokenFromAdapter
		edtTokenFrom.setOnClickListener {
			tokenFromSpinner.performClick()
		}
		imgArrowFrom.setOnClickListener {
			tokenFromSpinner.performClick()
		}
		edtTokenTo.setOnClickListener {
			tokenToSpinner.performClick()
		}
		imgArrowTo.setOnClickListener {
			tokenToSpinner.performClick()
		}
		tokenFromSpinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("Selected item position : $p2 On From")
					tokenFromAdapter.selectedPosition = p2
					tokenToSpinner.setSelection(if (p2 == 0) 1 else 0)
					updateTokenTxtViews(tokenFromAdapter, tokenToAdapter)
					initGetAddressLayoutUpdate()
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}
		tokenToSpinner.setSelection(1)
		tokenToSpinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("Selected item position : $p2 On To")
					tokenToAdapter.selectedPosition = p2
					tokenFromSpinner.setSelection(if (p2 == 0) 1 else 0)
					updateTokenTxtViews(tokenFromAdapter, tokenToAdapter)
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}

		imgSwap.setOnClickListener {
			val temp = tokenFromSpinner.selectedItemPosition
			tokenFromSpinner.setSelection(tokenToSpinner.selectedItemPosition)
			tokenToSpinner.setSelection(temp)
		}

	}

	private fun updateTokenTxtViews(
		fromAdapter: TokenSpinnerAdapter,
		toAdapter: TokenSpinnerAdapter
	) {
		binding.apply {
			edtTokenFrom.text = fromAdapter.dataOptions[fromAdapter.selectedPosition]
			edtTokenTo.text = toAdapter.dataOptions[toAdapter.selectedPosition]
			edtFromNetwork.text =
				if (fromAdapter.selectedPosition == 0) "Chia Network" else "TRC-20"
			edtToNetwork.text = if (toAdapter.selectedPosition == 0) "Chia Network" else "TRC-20"
		}
	}

	var containerBigger = true
	lateinit var anim: ValueAnimator

	private fun FragmentExchangeBinding.registerFilters() {
		val filterAmountEdtFrom = object : InputFilter {
			override fun filter(
				p0: CharSequence?,
				p1: Int,
				p2: Int,
				p3: Spanned?,
				p4: Int,
				p5: Int
			): CharSequence {
				if (p0 == null) return ""
				val curText = edtAmountFrom.text.toString()
				val locComo = curText.indexOf('.')
				val cursor = edtAmountFrom.selectionStart
				if (locComo == -1 || locComo == 0 || cursor <= locComo)
					return p0
				val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
				if (digitsAfterComo >= 2) {
					return ""
				}
				VLog.d("Cur input amount from : $p0")
				return p0
			}
		}
		edtAmountFrom.filters = arrayOf(filterAmountEdtFrom)
	}

	private fun initDetailTransaction(layout: View, viewClick: View, arrow: View) {

		VLog.d("Change View Height : $bigContainer on ExchangeFragment")
		viewClick.setOnClickListener {
			initAnimationCollapsingDetailTransaction(layout)
		}
		arrow.setOnClickListener {
			initAnimationCollapsingDetailTransaction(layout)
		}
		binding.apply {
			if (!containerBigger) {
				animManager.rotateBy180ForwardNoAnimation(
					imgArrowDownDetailTrans,
					getMainActivity()
				)
				val params = container.layoutParams
				params.height = getMainActivity().convertDpToPixel(bigContainer)
				container.layoutParams = params
			} else {
				val params = container.layoutParams
				params.height = getMainActivity().convertDpToPixel(smallContainer)
				container.layoutParams = params
			}
			if (hasOneFocusLeast) {
				viewDividerGetAddress.visibility = View.VISIBLE
				if (tokenToSpinner.selectedItem == 0) {
					layoutGetAddressUsdt.visibility = View.VISIBLE
					layoutGetAddressXch.visibility = View.GONE
				} else {
					layoutGetAddressUsdt.visibility = View.GONE
					layoutGetAddressXch.visibility = View.VISIBLE
				}
			}
		}

	}

	private var nextHeight = bigContainer
	fun initAnimationCollapsingDetailTransaction(layout: View) {
		if (this::anim.isInitialized && anim.isRunning)
			return
		val newHeightPixel = (nextHeight * resources.displayMetrics.density).toInt()
		anim = ValueAnimator.ofInt(layout.height, newHeightPixel)
		anim.duration = 500 // duration in milliseconds
		anim.interpolator = DecelerateInterpolator()
		anim.addUpdateListener {
			val value = it.animatedValue as Int
			layout.layoutParams.height = value
			layout.requestLayout()
		}
		anim.start()
		if (containerBigger) {
			animManager.rotateBy180Forward(binding.imgArrowDownDetailTrans, getMainActivity())
		} else
			animManager.rotateBy180Backward(binding.imgArrowDownDetailTrans, getMainActivity())
		containerBigger = !containerBigger
		if (containerBigger) {
			nextHeight = bigContainer
		} else {
			nextHeight = smallContainer
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		getAddressToSend()
		initChiaWalletAdapter()
		initFromTokenExchangeRequest()
	}

	private fun initGetAddressLayoutUpdate() {
		if (!hasOneFocusLeast) return
		VLog.d("Selected Item Token To Spinner : ${tokenToSpinner.selectedItemPosition}")
		binding.apply {
			if (tokenToSpinner.selectedItemPosition == 1) {
				layoutGetAddressXch.visibility = View.GONE
				layoutGetAddressUsdt.visibility = View.VISIBLE
				vm.requestExchangeRate("XCH")
			} else {
				vm.requestExchangeRate("USDT")
				layoutGetAddressXch.visibility = View.VISIBLE
				layoutGetAddressUsdt.visibility = View.GONE
			}
		}
	}

	private var requestExchangeJob: Job? = null
	fun initFromTokenExchangeRequest() {
		requestExchangeJob?.cancel()
		requestExchangeJob = lifecycleScope.launchWhenCreated {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				VLog.d("Started on exchange fragment has been called multiple times")
				vm.exchangeRequest.collectLatest {
					it?.let {
						when (it.state) {
							Resource.State.SUCCESS -> {
								val res = it.data!!
								VLog.d("Result of request exchange rate : $res")
								binding.initLimitToMinAndMax(res)
							}

							Resource.State.ERROR -> {
								VLog.d("Error has been called : ${it}")
								manageExceptionDialogsForRest(
									getMainActivity(),
									dialogManager,
									it.error
								)
							}
						}
					}
				}
			}
		}
	}

	private fun FragmentExchangeBinding.initLimitToMinAndMax(res: ExchangeRate) {
		edtAmountFrom.addTextChangedListener {
			val amount = StringBuilder(it.toString())
			if (amount.toString().toDoubleOrNull() == null) {
				amount.append(0, '0')
			}
			val double = amount.toString().toDoubleOrNull() ?: 0.0
			constraintCommentMinAmount.visibility = if (double == 0.0) View.VISIBLE else View.GONE
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		requestExchangeJob?.cancel()
		VLog.d("On Destroy view  called on exchange fragment")
	}

	override fun onDestroy() {
		super.onDestroy()
		VLog.d("On Destroy called on exchange fragment")
	}


}
