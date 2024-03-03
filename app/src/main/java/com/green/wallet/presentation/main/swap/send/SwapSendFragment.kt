package com.green.wallet.presentation.main.swap.send

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.webkit.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.green.wallet.R
import com.example.common.tools.*
import com.google.gson.Gson
import com.green.compose.custom.fee.FeeContainer
import com.green.compose.dimens.size_10
import com.green.compose.dimens.text_16
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.databinding.FragmentSendSwapBinding
import com.green.wallet.domain.domainmodel.TokenWallet
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.custom.base.BaseFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.pincode.PinCodeFragment
import com.green.wallet.presentation.main.send.WalletListAdapter
import com.green.wallet.presentation.tools.*
import com.green.wallet.presentation.viewBinding
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.dialog_confirm_transactions_coins.*
import kotlinx.android.synthetic.main.dialog_notification_detail.*
import kotlinx.android.synthetic.main.fragment_impmnemonic.*
import kotlinx.android.synthetic.main.fragment_listing.*
import kotlinx.android.synthetic.main.fragment_listing.txtBlockChain
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.android.synthetic.main.fragment_receive.txtAddress
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject


class SwapSendFragment : BaseFragment() {

    private val binding by viewBinding(FragmentSendSwapBinding::bind)
    private lateinit var tokenAdapter: DynamicSpinnerAdapter
    private lateinit var walletAdapter: WalletListAdapter

    @Inject
    lateinit var dialogManager: DialogManager

    private var curNetworkType: String = "Chia Network"
    private var curFingerPrint: Long? = null

    companion object {
        const val SEND_ADDRESS_KEY = "send_address_key"
        const val SEND_AMOUNT_KEY = "send_amount_key"
    }

    private var sendAddress: String = ""
    private var sendAmount: Double = 0.0

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SwapSendViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var anim: AnimationManager

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    private var updateJob: Job? = null
    private var sendTransJob: Job? = null
    private var addressAlreadyExist: Job? = null
    private var commissionJob: Job? = null
    private var genSpendBundleJob: Job? = null


    private var curSendAddressWallet: String = ""
    private var spendableAmountToken: Double = 0.0
    private var spendableAmountFee: Double = 0.0
    private var chosenTokenCode: String = ""
    private var walletAdapterPosition = -1
    private var tokendAdapterPosition = 0
    private var lastTokenBalanceText = "0.0 XCH"
    private var lastTokenBalanceTxtInUSDT = "0.0 USD"

    private val handler = CoroutineExceptionHandler { _, ex ->
        VLog.d("Exception caught on send fragment : ${ex.message} ")
    }

    private var curTokenWalletList = listOf<TokenWallet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sendAddress = it.getString(SEND_ADDRESS_KEY, "")
            sendAmount = it.getDouble(SEND_AMOUNT_KEY, 0.0)
            viewModel.updateSendAmount(sendAmount)
        }
        VLog.d("On Create SendFragment Params : FingerPrint  ${curFingerPrint}, NetworkType : $curNetworkType")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_send_swap, container, false)
        VLog.d("OnCreateView on send Fragment")
        curActivity().sendCoinsFragmentView = view
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerCLicks()
        getAddressToSend()
        queryWalletList(curNetworkType, null)
        updateNetworkName(curNetworkType)
        VLog.d("OnViewCreated on send Swap Fragment")
        initSwipeRefreshLayout()
        binding.updateParams()
        initFeeBlockCommission()
    }

    private fun initFeeBlockCommission() {
        binding.composeFeeBlock.setContent {

            val state by viewModel.viewState.collectAsStateWithLifecycle()

            GreenWalletTheme {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    FeeContainer(
                        normal = state.dexieFee,
                        isEnough = state.isFeeEnough,
                        fee = {
                            viewModel.updateFee(it)
                        }
                    )

                    if (!state.isFeeEnough) {
                        DefaultText(
                            text = stringResource(id = R.string.send_token_insufficient_funds_error) + " XCH",
                            size = text_16,
                            color = Provider.current.errorColor,
                            modifier = Modifier.padding(top = size_10)
                        )
                    }
                }
            }
        }
    }

    private fun initConstrainsAfterCommobasedOnToken() {
        val sendingToken = tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
        val commissionToken = tokenAdapter.dataOptions[0]
        val precisionAfterCommonToken = getTokenPrecisionAfterComoByTokenCode(sendingToken)
        val filterSendingAmountEdt = object : InputFilter {
            override fun filter(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Spanned?,
                p4: Int,
                p5: Int
            ): CharSequence {
                if (p0 == null) return ""
                val curText = binding.edtEnterAmount.text.toString()
                val locComo = curText.indexOf('.')
                val cursor = binding.edtEnterAmount.selectionStart
                if (locComo == -1 || locComo == 0 || cursor <= locComo)
                    return p0
                val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
                if (digitsAfterComo >= precisionAfterCommonToken) {
                    return ""
                }
                return p0
            }
        }
        binding.apply {
            edtEnterAmount.filters = arrayOf(filterSendingAmountEdt)
        }
    }

    private var swipeJob: Job? = null

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.apply {
            setOnRefreshListener {
                if (connectionLiveData.isOnline) {
                    swipeJob?.cancel()
                    swipeJob = lifecycleScope.launch {
                        val job = launch {
                            viewModel.swipedRefreshLayout {
                                VLog.d("Is refreshing false on send fragment :")
                            }
                        }
                        job.join()
                        isRefreshing = false
                    }
                } else {
                    isRefreshing = false
                    dialogManager.showNoInternetTimeOutExceptionDialog(curActivity()) {

                    }
                }
            }
            setColorSchemeResources(R.color.green)
        }
    }

    private var spendableBalanceJob: Job? = null

    @SuppressLint("SetTextI18n")
    private fun calculateSpendableBalance() {
        spendableBalanceJob?.cancel()
        spendableBalanceJob = lifecycleScope.launch(handler) {
            val curSendingToken = tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
            val curWallet = walletAdapter.walletList[walletAdapterPosition]
            withContext(Dispatchers.IO) {
                viewModel.getSpentCoinsAmountsAddressCodeForSpendableBalance(
                    address = curWallet.address,
                    tokenCode = curSendingToken,
                    networkType = curWallet.networkType
                ).collectLatest { sentTokenMempoolAmounts: DoubleArray ->
//				VLog.d("Address : ${curWallet.address} , SentToken : $curSendingToken  Amounts : ${sentTokenMempoolAmounts.toList()}")
                    withContext(Dispatchers.Main) {
                        val initialAmountToken =
                            curWallet.tokenWalletList[tokenAdapter.selectedPosition].amount
                        val initialAmountNetworkTypeToken =
                            curWallet.tokenWalletList[0].amount
                        val txtSpendableBalance =
                            curActivity().getStringResource(R.string.spendable_balance)
                        spendableAmountToken = initialAmountToken - sentTokenMempoolAmounts[0]
                        if (spendableAmountToken < 0.0)
                            spendableAmountToken = 0.0
                        spendableAmountFee =
                            initialAmountNetworkTypeToken - sentTokenMempoolAmounts[1]
                        if (spendableAmountFee < 0.0)
                            spendableAmountFee = 0.0
                        var bigDecimalSpendableAmount =
                            (BigDecimal("$initialAmountToken").subtract(BigDecimal("${sentTokenMempoolAmounts[0]}"))).toDouble()
                        if (bigDecimalSpendableAmount < 0.0)
                            bigDecimalSpendableAmount = 0.0
                        var spendableAmountString =
                            formattedDoubleAmountWithPrecision(bigDecimalSpendableAmount)

                        if (Math.round(bigDecimalSpendableAmount)
                                .toDouble() == bigDecimalSpendableAmount || bigDecimalSpendableAmount == 0.0
                        )
                            spendableAmountString = "${Math.round(bigDecimalSpendableAmount)}"

                        val bigDecimalSpendableFee =
                            formattedDoubleAmountWithPrecision(
                                (BigDecimal("$initialAmountNetworkTypeToken").subtract(
                                    BigDecimal("${sentTokenMempoolAmounts[1]}")
                                )).toDouble()
                            )
                        binding.apply {
                            txtSpendableBalanceAmount.setText(
                                "$txtSpendableBalance: $spendableAmountString"
                            )
                        }
                        initAmountNotEnoughWarnings()
                        initConstrainsAfterCommobasedOnToken()
                    }
                }
            }
        }
    }

    private fun removeGADIfCurNetworkChives(networkType: String) {
        val enterAmount = binding.edtEnterAmount.text.toString().toDoubleOrNull()
        if (enterAmount != null) {
            convertAmountToUSDGAD(enterAmount)
        }
    }

    private fun regulateVisibilityOfTxtsAfterPasscode() {
        binding.apply {
            if (edtEnterAmount.text.toString().isNotEmpty()) {
                txtEnterAmount.visibility = View.VISIBLE
                txtShortNetworkType.text = "$chosenTokenCode"
            }
            if (edtAddressWallet.text.toString().isNotEmpty())
                txtEnterAddressWallet.visibility = View.VISIBLE
            txtWalletAmount.text = lastTokenBalanceText
        }
    }


    private fun queryWalletList(type: String, fingerPrint: Long?) {
        removeGADIfCurNetworkChives(type)
        updateJob?.cancel()
        updateJob = lifecycleScope.launch(handler) {

            viewModel.queryWalletWithTokensList(type, fingerPrint)
                .collectLatest { walletTokenList ->

                    for (walletWithToken in walletTokenList) {
                        VLog.d("Wallet Token on SwapSendFragment : $walletWithToken ")
                    }
                    walletAdapter = WalletListAdapter(curActivity(), walletTokenList)
                    binding.walletSpinner.adapter = walletAdapter
                    if (walletAdapterPosition == -1) {
                        walletAdapterPosition =
                            walletTokenList.map { it.fingerPrint }.indexOf(curFingerPrint)
                    }
                    if (walletAdapterPosition < walletTokenList.size && walletAdapterPosition >= 0) {
                        binding.walletSpinner.setSelection(walletAdapterPosition)
                        walletAdapter.selectedPosition = walletAdapterPosition
                    }
                    binding.walletSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                VLog.d("Select In Wallet Adapter : $p2")
                                walletAdapter.selectedPosition = p2
                                walletAdapterPosition = p2
                                initCurWalletDetails(walletAdapter.walletList[p2])
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }

                        }
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initCurWalletDetails(wallet: WalletWithTokens) {
        txt_network_name.text = wallet.networkType.split(" ")[0]
        txtHiddenPublicKey.text = hideFingerPrint(wallet.fingerPrint)
        viewModel.updateSpendableBalance(wallet)
        updateTokensSpinner(wallet.tokenWalletList)
        verifySendingAddress(wallet.address)
    }

    private fun updateTokensSpinner(tokenWalletList: List<TokenWallet>) {
        curTokenWalletList = tokenWalletList
        tokenAdapter = DynamicSpinnerAdapter(
            widthDp = 100,
            context = curActivity(),
            tokenWalletList.filter {
                (it.code == "XCH" || it.asset_id.trim().isNotEmpty() || it.code == "XCC")
            }
                .map { it.code }.toMutableList()
        )
        binding.tokenSpinner.adapter = tokenAdapter
        if (tokendAdapterPosition < tokenWalletList.size) {
            binding.tokenSpinner.setSelection(tokendAdapterPosition)
            tokenAdapter.selectedPosition = tokendAdapterPosition
        }
        binding.tokenSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    tokenAdapter.selectedPosition = p2
                    tokendAdapterPosition = p2
                    checkingPrecisionsForEnterAmount()
                    calculateSpendableBalance()
                    updateAmounts(tokenWalletList[p2])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

    }

    private fun checkingPrecisionsForEnterAmount() {
        if (tokenAdapter.selectedPosition != 0) {
            val builder = StringBuilder()
            val curText = binding.edtEnterAmount.text.toString()
            var at = 0
            while (at < curText.length && curText[at] != '.') {
                builder.append(curText[at])
                at++
            }
            if (at != curText.length) {
                builder.append(curText[at++])
                val till = Math.min(curText.length, at + 3)
                while (at < till) {
                    builder.append(curText[at])
                    at++
                }
            }
            if (builder.toString() != curText) {
                binding.edtEnterAmount.apply {
                    setText(builder.toString())
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAmounts(tokenWallet: TokenWallet) {
        binding.txtWalletAmount.text =
            formattedDoubleAmountWithPrecision(tokenWallet.amount) + " ${tokenWallet.code}"
        lastTokenBalanceText = txtWalletAmount.text.toString()
        val formattedBalance = String.format("%.2f", tokenWallet.amountInUSD).replace(",", ".")
        lastTokenBalanceTxtInUSDT = "⁓$formattedBalance USD"
        chosenTokenCode = tokenWallet.code
        if (txtShortNetworkType.text.toString().length > 1) {
            txtShortNetworkType.text = tokenWallet.code
        }
        if (binding.edtEnterAmount.text.toString().isNotEmpty()) {
            val amount = binding.edtEnterAmount.text.toString().toDouble()
            convertAmountToUSDGAD(amount)
        }
    }

    private var passcodeJob: Job? = null

    private fun sendTransaction(
        spendBundleJSON: String,
        url: String,
        amount: Double,
        networkType: String,
        fingerPrint: Long,
        dest_puzzle_hash: String,
        address: String,
        spentCoinsJson: String,
        spentConisToken: String
    ) {
        sendTransJob?.cancel()
        sendTransJob = lifecycleScope.launch {
            val res =
                viewModel.push_transaction(
                    spendBundleJSON,
                    url,
                    amount,
                    networkType,
                    fingerPrint,
                    chosenTokenCode,
                    dest_puzzle_hash,
                    address = address,
                    viewModel.viewState.value.fee,
                    spentCoinsJson,
                    spentConisToken
                )
            when (res.state) {
                Resource.State.SUCCESS -> {
                    dialogManager.hidePrevDialogs()
                    showSuccessSendMoneyDialog()
                }

                Resource.State.ERROR -> {
                    val error = res.error!!
                    manageExceptionDialogsForBlockChain(curActivity(), dialogManager, error)
                }

                Resource.State.LOADING -> {

                }
            }

        }
    }

    private fun initFlutterToGenerateSpendBundle(
        dest_address: String
    ) {
        genSpendBundleJob?.cancel()
        genSpendBundleJob = lifecycleScope.launch(handler) {
            val wallet = walletAdapter.walletList[walletAdapterPosition]
            val asset_id = curTokenWalletList[tokendAdapterPosition].asset_id
            val curCode = curTokenWalletList[tokendAdapterPosition].code
            var precision =
                if (isThisChivesNetwork(wallet.networkType)) Math.pow(
                    10.0,
                    8.0
                ).toLong() else Math.pow(
                    10.0,
                    12.0
                ).toLong()
            if (asset_id.isNotEmpty())
                precision = 1000
            val amount = (Math.round(getDoubleValueFromEdt(binding.edtEnterAmount) * precision))
            val fee =
                Math.round(
                    viewModel.viewState.value.fee * if (isThisChivesNetwork(
                            wallet.networkType
                        )
                    ) Math.pow(
                        10.0,
                        8.0
                    ) else Math.pow(
                        10.0,
                        12.0
                    )
                )
            val mnemonicString = convertListToStringWithSpace(wallet.mnemonics)
            val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
            val alreadySpentCoins =
                viewModel.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, curCode)
            val methodChannel = MethodChannel(
                (curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
                METHOD_CHANNEL_GENERATE_HASH
            )
            listenToCreatingSpendBundleRes(
                url,
                getDoubleValueFromEdt(binding.edtEnterAmount),
                wallet.networkType,
                wallet.fingerPrint,
                wallet.address
            )
            val argSpendBundle = hashMapOf<String, Any>()
            argSpendBundle["fee"] = fee
            argSpendBundle["amount"] = amount
            argSpendBundle["mnemonicString"] = mnemonicString
            argSpendBundle["url"] = url
            argSpendBundle["dest"] = dest_address
            argSpendBundle["network_type"] = convertNetworkTypeForFlutter(wallet.networkType)
            argSpendBundle["asset_id"] = asset_id
            argSpendBundle["spentCoins"] = Gson().toJson(alreadySpentCoins)
            argSpendBundle["observer"] = wallet.observer
            argSpendBundle["nonObserver"] = wallet.nonObserver
            VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
            if (asset_id.trim().isEmpty() && (curCode == "XCH" || curCode == "XCC"))
                methodChannel.invokeMethod("generateSpendBundleXCH", argSpendBundle)
            else {
                methodChannel.invokeMethod("generateSpendBundleToken", argSpendBundle)
                if (asset_id.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Asset ID is empty", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private suspend fun listenToCreatingSpendBundleRes(
        url: String,
        amount: Double,
        networkType: String,
        fingerPrint: Long,
        address: String
    ) {
        val methodChannel = MethodChannel(
            (curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL_GENERATE_HASH
        )
        methodChannel.setMethodCallHandler { method, callBack ->
            if (method.method == "getSpendBundle") {
                val spendBundleJson =
                    (method.arguments as HashMap<*, *>)["spendBundle"].toString()
                val dest_puzzle_hash =
                    (method.arguments as HashMap<*, *>)["dest_puzzle_hash"].toString()
                val spentCoinsJson =
                    (method.arguments as HashMap<*, *>)["spentCoins"].toString()
                val spentTokensJson = (method.arguments as HashMap<*, *>)["spentTokens"] ?: ""
                VLog.d("SpentCoins Json for sending trans : $spentCoinsJson")
                sendTransaction(
                    spendBundleJson,
                    url,
                    amount,
                    networkType,
                    fingerPrint,
                    dest_puzzle_hash,
                    address,
                    spentCoinsJson,
                    spentTokensJson.toString()
                )
            } else if (method.method == "exception") {
                showFailedSendingTransaction()
                Toast.makeText(
                    curActivity(),
                    "Exception : ${method.arguments}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
        val item = prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
        if (item.isEmpty()) return null
        return gson.fromJson(item, NetworkItem::class.java)
    }


    private fun getDoubleValueFromEdt(edt: TextView): Double {
        return edt.text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun showSuccessSendMoneyDialog() {
        getMainActivity().apply {
            if (!this.isDestroyed) {
                dialogManager.showSuccessDialog(
                    this,
                    getStringResource(R.string.send_token_pop_up_succsess_title),
                    getStringResource(R.string.send_token_pop_up_succsess_description),
                    getStringResource(R.string.ready_btn),
                    isDialogOutsideTouchable = false
                ) {
                    Handler(Looper.myLooper()!!).postDelayed({
                        popBackStackOnce()
                    }, 500)
                }
            }
        }

    }

    private fun showFailedSendingTransaction() {
        dialogManager.hidePrevDialogs()
        curActivity().apply {
            if (!this.isFinishing) {
                dialogManager.showFailureDialog(
                    this,
                    getStringResource(R.string.pop_up_failed_error_title),
                    getStringResource(R.string.send_token_pop_up_transaction_fail_error_description),
                    getStringResource(R.string.pop_up_failed_error_return_btn)
                ) {

                }
            }
        }
    }

    private fun verifySendingAddress(address: String) {

    }

    private var prevEnterAddressJob: Job? = null

    private fun getAddressToSend() {
        prevEnterAddressJob?.cancel()
        prevEnterAddressJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    curActivity().mainViewModel.decodedQrCode.collectLatest {
                        if (it.isNotEmpty()) {
                            binding.edtAddressWallet.setText(it)
                            curActivity().mainViewModel.saveDecodeQrCode("")
                        }
                    }
                }
                curActivity().mainViewModel.chosenAddress.collectLatest { addres ->
                    VLog.d("Chosen Address from AddressFragment : $addres")
                    if (addres.isNotEmpty()) {
                        binding.edtAddressWallet.setText(addres)
                        curActivity().mainViewModel.saveChosenAddress("")
                    }
                }
            }
        }
    }


    private fun FragmentSendSwapBinding.updateParams() {
        edtAddressWallet.text = sendAddress
        edtEnterAmount.text = formattedDoubleAmountWithPrecision(sendAmount)
    }

    private fun FragmentSendSwapBinding.registerCLicks() {

        backLayout.setOnClickListener {
            curActivity().popBackStackOnce()
        }


//		edtAddressWallet.addTextChangedListener {
//			if (it.isNullOrEmpty()) {
//				twoEdtsFilled.remove(1)
//			} else {
//				twoEdtsFilled.add(1)
//				txtEnterAddressWallet.visibility = View.VISIBLE
//				edtAddressWallet.hint = ""
//				line2.setBackgroundColor(curActivity().getColorResource(R.color.green))
//				edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
//				txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.green))
//			}f
//			enableBtnContinueTwoEdtsFilled()
//		}

        edtEnterAmount.addTextChangedListener {
            kotlin.runCatching {
                initAmountNotEnoughWarnings()
            }.onFailure {
                VLog.d("Excepting entering amount into amount edts : ${it}")
            }
        }

        btnContinue.setOnClickListener {
            if (sendingBtnWaitingJob != null && sendingBtnWaitingJob!!.isActive)
                return@setOnClickListener
            sendingBtnWaitingJob = lifecycleScope.launch {
                delay(1000L)
            }
            if (connectionLiveData.isOnline) {
                val addressWalletInValid = binding.edtAddressWallet.text.toString().isEmpty()
                if (addressWalletInValid) {
                    showWarningInvalidWalledAddress()
                    return@setOnClickListener
                }
                showConfirmTransactionDialog()
            } else {
                dialogManager.showNoInternetTimeOutExceptionDialog(curActivity()) {

                }
            }
        }
        icWalletList.setOnClickListener {
            binding.walletSpinner.performClick()
        }


        chosenNetworkRel.setOnClickListener {
            binding.networkSpinner.performClick()
        }

        curActivity().mainViewModel.sendMoneyFalse()

        anim.animateArrowIconCustomSpinner(
            binding.walletSpinner,
            binding.icWalletList,
            curActivity()
        )
        anim.animateArrowIconCustomSpinner(binding.networkSpinner, imgIconSpinner, curActivity())

    }

    private var sendingBtnWaitingJob: Job? = null


    @SuppressLint("SetTextI18n")
    private fun convertAmountToUSDGAD(amount: Double) {
        lifecycleScope.launch(handler) {

        }
    }

    private fun checkAddressAlreadyExistInDB() {
        addressAlreadyExist?.cancel()
        addressAlreadyExist = lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val addressList =
                    viewModel.checkIfAddressExistInDb(binding.edtAddressWallet.text.toString())
                VLog.d("AddressList by given address :  ${addressList.size}")
                withContext(Dispatchers.Main) {
                    if (addressList.isNotEmpty()) {
                        txtAddressAlredyExistWarning.visibility = View.VISIBLE
                    } else {
                        txtAddressAlredyExistWarning.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun showWarningInvalidWalledAddress() {
        binding.apply {
            edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
            txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
            txtAddressDontExistWarning.visibility = View.VISIBLE
            lifecycleScope.launch {
                delay(2000)
                if (!this@SwapSendFragment.isVisible) {
                    edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.greey))
                    txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.greey))
                    txtAddressDontExistWarning.visibility = View.GONE
                }
            }
        }
    }

    private fun initAmountNotEnoughWarnings() {
        val amountSending = binding.edtEnterAmount.text.toString().toDoubleOrNull()
        if (amountSending == null || amountSending == 0.0) {
            hideNotEnoughAmountWarningSending()
            hideAmountNotEnoughWarning()
            convertAmountToUSDGAD(0.0)
        }
        val amountFee = viewModel.viewState.value.fee
        if (amountFee == null || amountFee == 0.0) {
            hideAmountNotEnoughWarning()
        }
        var isSendingAmountEnough = true
        if (amountSending != null && amountSending != 0.0) {
            convertAmountToUSDGAD(amountSending)
            val total =
                amountSending + if (tokenAdapter.selectedPosition != 0) 0.0 else (amountFee ?: 0.0)
            if (total > spendableAmountToken) {
                showNotEnoughAmountWarning()
                notEnoughAmountWarningSending()
                isSendingAmountEnough = false
            } else {
                hideAmountNotEnoughWarning()
                hideNotEnoughAmountWarningSending()
            }
        }
        if (amountFee != null && amountFee != 0.0) {
            val total =
                amountFee + if (tokenAdapter.selectedPosition != 0) 0.0 else (amountSending ?: 0.0)
            if (total > spendableAmountFee) {
                if (tokenAdapter.selectedPosition == 0) {
                    showNotEnoughAmountWarning()
                    notEnoughAmountWarningSending()
                }
            } else {
                if (isSendingAmountEnough)
                    hideAmountNotEnoughWarning()
            }
        }
    }

    private fun showNotEnoughAmountWarning() {
        binding.apply {
            val token = getTokenTypeThatIsNotEnough()
            val text =
                curActivity().getStringResource(R.string.send_token_insufficient_funds_error) + " $token"
            txtNotEnoughMoneyWarning.text = text
            txtNotEnoughMoneyWarning.visibility = View.VISIBLE
        }
    }

    private fun notEnoughAmountWarningSending() {
        binding.apply {
            edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
            txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
        }
    }

    private fun hideNotEnoughAmountWarningSending() {
        binding.apply {
            edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.greey))
            txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.greey))
        }
    }

    private fun getTokenTypeThatIsNotEnough(): String {
        binding.apply {
            val enteredAmount = edtEnterAmount.text.toString().toDoubleOrNull() ?: 0.0
            var commissionAmount = viewModel.viewState.value.fee
            //check only token first
            if (tokenAdapter.selectedPosition != 0) {
                if (enteredAmount > spendableAmountToken && this@SwapSendFragment::tokenAdapter.isInitialized)
                    return tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
            }
        }
        return getShortNetworkType(curNetworkType)
    }

    private fun hideAmountNotEnoughWarning() {
        binding.apply {
            edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.greey))
            txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.greey))
            txtNotEnoughMoneyWarning.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNetworkName(coinType: String) {
        VLog.d("Selected CoinType : $coinType")
        binding.apply {
            txtChosenNetwork.text = coinType
        }
    }

    private fun showConfirmTransactionDialog() {
        val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_confirm_transactions_coins)
        registerBtnClicks(dialog)
        initConfirmDialogDetails(dialog)
        val width = resources.displayMetrics.widthPixels
        dialog.apply {
            addingDoubleDotsTxt(txtToken)
            addingDoubleDotsTxt(txtBlockChain)
            addingDoubleDotsTxt(txtAmount)
            addingDoubleDotsTxt(txtAddress)
            addingDoubleDotsTxt(txtCommissionConfirm)
        }
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun initConfirmDialogDetails(dialog: Dialog) {
        dialog.apply {
            findViewById<TextView>(R.id.edtConfirmToken).setText(tokenAdapter.dataOptions[tokenAdapter.selectedPosition])
            findViewById<TextView>(R.id.edtConfirmNetwork).setText(walletAdapter.walletList[walletAdapter.selectedPosition].networkType)
            findViewById<TextView>(R.id.edtConfirmWalletAmount).setText(binding.edtEnterAmount.text.toString())
            var commissionText = viewModel.viewState.value.fee.toString()
            if (commissionText.isEmpty())
                commissionText = "0"
            findViewById<TextView>(R.id.edtConfirmWalletCommision).setText("$commissionText")
            findViewById<TextView>(R.id.edtConfirmAddressWallet).setText(binding.edtAddressWallet.text.toString())
        }
    }


    @SuppressLint("SetTextI18n")
    private fun addingDoubleDotsTxt(txt: TextView) {
        txt.apply {
            txt.text = "${txt.text}:"
        }
    }

    private fun registerBtnClicks(dialog: Dialog) {
        dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            it.startAnimation(anim.getBtnEffectAnimation())
            dialog.dismiss()
            PinCodeFragment.build(reason = ReasonEnterCode.SEND_TRANSACTION)
                .show(childFragmentManager, "")
        }
        dialog.findViewById<LinearLayout>(R.id.back_layout).setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun collectFlowOnStarted(scope: CoroutineScope) {
        viewModel.viewState.collectFlow(scope) {
            initBtnState(it)
        }
        viewModel.event.collectFlow(scope) {
            when (it) {
                SwapSendEvent.PinConfirmed -> {
                    dialogManager.showProgress(requireActivity())
                    initFlutterToGenerateSpendBundle(
                        binding.edtAddressWallet.text.toString().lowercase()
                    )
                }
            }
        }
    }

    private fun initBtnState(it: SwapSendViewState) {
        binding.btnContinue.isEnabled = it.isFeeEnough
    }

    override fun onStart() {
        super.onStart()
        VLog.d("SendFragment onStart")
        regulateVisibilityOfTxtsAfterPasscode()
        if (dialogManager.isProgressDialogShowing() == true) {
            dialogManager.hidePrevDialogs()
            dialogManager.showProgress(curActivity())
        }
    }

    override fun onResume() {
        super.onResume()
        VLog.d("SendFragment onResume")
    }


    override fun onPause() {
        super.onPause()
        VLog.d("SendFragment onPause")
    }

    override fun onStop() {
        super.onStop()
        VLog.d("SendFragment onStop")
        sendTransJob?.cancel()
        swipeJob?.cancel()
    }


    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it ->
            it.entries.forEach {
                if (it.value) {
                    curActivity().mainViewModel.saveDecodeQrCode("")
                    curActivity().move2ScannerFragment(null, null)
                } else
                    Toast.makeText(
                        curActivity(),
                        curActivity().getStringResource(R.string.camera_permission_missing),
                        Toast.LENGTH_SHORT
                    )
                        .show()
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        curActivity().sendCoinsFragmentView = null
        updateJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogManager.dismissAllPrevDialogs()
    }

    private fun curActivity() = requireActivity() as MainActivity


}
