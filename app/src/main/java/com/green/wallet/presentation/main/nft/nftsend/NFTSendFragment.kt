package com.green.wallet.presentation.main.nft.nftsend

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.common.tools.addingDoubleDotsTxt
import com.example.common.tools.formatString
import com.google.gson.Gson
import com.green.compose.custom.fee.FeeContainer
import com.green.compose.dimens.size_10
import com.green.compose.dimens.text_12
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.data.network.dto.coins.CoinRecord
import com.green.wallet.databinding.FragmentSendNftBinding
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.custom.base.BaseFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.nft.nftdetail.NFTDetailsFragment
import com.green.wallet.presentation.main.send.TransferState
import com.green.wallet.presentation.tools.*
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.dialog_confirm_send_nft.*
import kotlinx.android.synthetic.main.fragment_send.txtAddressAlredyExistWarning
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


class NFTSendFragment : BaseFragment() {

    private lateinit var binding: FragmentSendNftBinding

    private val twoEdtsFilled = mutableSetOf<Int>()

    companion object {
        const val NFT_KEY = "nft_key"
    }

    lateinit var nftInfo: NFTInfo

    @Inject
    lateinit var factory: ViewModelFactory
    private val vm: NFTSendViewModel by viewModels { factory }

    @Inject
    lateinit var dialogManager: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val nftInfo = it.getParcelable<NFTInfo>(NFTDetailsFragment.NFT_KEY)
            this.nftInfo = nftInfo!!
            VLog.d("NFT Details on Fragment : $nftInfo")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSendNftBinding.inflate(inflater)
        getMainActivity().sendNftFragmentView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerClicks()
        binding.updateViews()
        initFeeBlock()
        getQrCodeDecoded()
        vm.initNFTCoin(nftInfo)
    }

    private fun initFeeBlock() {
        binding.composeFeeBlock.setContent {

            val state by vm.viewState.collectAsState()

            GreenWalletTheme {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    DefaultText(
                        text = "Spendable Balance: ${formattedDoubleAmountWithPrecision(state.spendableBalance)}",
                        size = text_12,
                        color = if (state.feeEnough) Provider.current.greyText else Provider.current.errorColor,
                        modifier = Modifier.padding(
                            bottom = size_10
                        )
                    )

                    FeeContainer(
                        normal = state.dexieFee,
                        isEnough = state.feeEnough,
                        fee = {
                            vm.updateChosenFee(it)
                        }
                    )
                }
            }
        }
    }

    var sendJobState: Job? = null

    private fun pushTransactionState() {
        sendJobState?.cancel()
        sendJobState = lifecycleScope.launch {
            vm.sendNFTState.collectLatest {
                it?.let {
                    when (it.state) {
                        Resource.State.SUCCESS -> {
                            dialogManager.hidePrevDialogs()
                            insertAddressEntityIfBoxChecked()
                            showSuccessSendMoneyDialog()
                        }

                        Resource.State.ERROR -> {
                            dialogManager.hidePrevDialogs()
                            val error = it.error
                            manageExceptionDialogsForBlockChain(
                                getMainActivity(),
                                dialogManager,
                                error
                            )
                            sendJobState?.cancel()
                        }

                        Resource.State.LOADING -> {

                        }
                    }
                }
            }
        }
    }

    private suspend fun insertAddressEntityIfBoxChecked() {
        if (binding.checkBoxAddAddress.isChecked) {
            val name = binding.edtAddressName.text.toString()
            val existAddress =
                vm.checkIfAddressExistInDb(binding.edtAddressWallet.text.toString())
            if (existAddress.isEmpty())
                vm.insertAddressEntity(
                    Address(
                        UUID.randomUUID().toString(),
                        binding.edtAddressWallet.text.toString(),
                        name,
                        "",
                        System.currentTimeMillis()
                    )
                )
        }
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
                        popBackStackOnce()
                    }, 500)
                }
            }
        }

    }

    fun FragmentSendNftBinding.registerClicks() {

        checkBoxAddAddress.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                addNameAddressLay.visibility = if (p1) View.VISIBLE else View.GONE
                txtAddressAlredyExistWarning.visibility = if (p1) View.VISIBLE else View.GONE
                checkBoxAddAddress.setTextColor(
                    if (p1) ContextCompat.getColor(
                        getMainActivity(),
                        R.color.green
                    ) else ContextCompat.getColor(getMainActivity(), R.color.checkbox_text_color)
                )
                if (p1)
                    checkAddressAlreadyExistInDB()
            }
        })

        backLayout.setOnClickListener {
            getMainActivity().popBackStackOnce()
        }


        edtAddressWallet.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                twoEdtsFilled.remove(1)
            } else {
                twoEdtsFilled.add(1)
                txtEnterAddressWallet.visibility = View.VISIBLE
                edtAddressWallet.hint = ""
                line2.setBackgroundColor(getMainActivity().getColorResource(R.color.green))
                edtAddressWallet.setTextColor(getMainActivity().getColorResource(R.color.secondary_text_color))
                txtEnterAddressWallet.setTextColor(getMainActivity().getColorResource(R.color.green))
            }
            vm.updateDestAddress(it.toString())
        }

        edtAddressWallet.setOnFocusChangeListener { view, focus ->
            if (focus) {
                txtEnterAddressWallet.visibility = View.VISIBLE
                edtAddressWallet.hint = ""
                line2.setBackgroundColor(getMainActivity().getColorResource(R.color.green))
            } else if (edtAddressWallet.text.toString().isEmpty()) {
                txtEnterAddressWallet.visibility = View.INVISIBLE
                edtAddressWallet.hint = getMainActivity().getString(R.string.send_token_adress)
            }
            if (!focus) {
                line2.setBackgroundColor(getMainActivity().getColorResource(R.color.edt_divider))
            }
        }

        btnContinue.setOnClickListener {
            showConfirmTransactionDialog()
        }

        imgAddressIc.setOnClickListener {
            getMainActivity().move2AddressFragmentList(true)
        }

        imgEdtScan.setOnClickListener {
            requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
        }

        imgCopyNftId.setOnClickListener {
            copyToClipBoardShowCopied(nftInfo.nft_id)
        }

    }

    private fun FragmentSendNftBinding.updateViews() {
        edtNFTName.text = nftInfo.name
        edtNFTCollection.text = nftInfo.collection
        edtNftID.text = formatString(15, nftInfo.nft_id, 4)
        var firstView = true
        for ((key, value) in nftInfo.properties) {
            containerProperties.addView(generateLinearLayoutProperties(firstView, key, value))
            firstView = false
        }

        Glide.with(getMainActivity()).load(nftInfo.data_url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imgCardNft.visibility = View.VISIBLE
                    frameProgressBar.visibility = View.GONE
                    return false
                }

            })
            .into(imgNft)

    }

    private var addressAlreadyExist: Job? = null
    private fun checkAddressAlreadyExistInDB() {
        addressAlreadyExist?.cancel()
        addressAlreadyExist = lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val addressList =
                    vm.checkIfAddressExistInDb(binding.edtAddressWallet.text.toString())
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

    private fun initAddressLookUp(it: NftSendViewState) {
        binding.apply {
            if (it.namesDao == null) {
                edtAddressWallet.setTextColor(requireActivity().getColorResource(R.color.secondary_text_color))
                txtEnterAddressWallet.setTextColor(requireActivity().getColorResource(R.color.green))
                addressNamesLay.visibility = View.GONE
                return@apply
            }
            addressNamesLay.visibility = View.VISIBLE

            txtEnterAddressWallet.setTextColor(
                requireActivity().getColorResource(
                    if (it.namesDao.isNotEmpty())
                        R.color.green
                    else
                        R.color.red_mnemonic
                )
            )

            edtAddressWallet.setTextColor(
                requireActivity().getColorResource(
                    if (it.namesDao.isNotEmpty())
                        R.color.secondary_text_color
                    else
                        R.color.red_mnemonic
                )
            )

            txtCoinsWillBeSent.setTextColor(
                requireActivity().getColorResource(
                    if (it.namesDao.isNotEmpty())
                        R.color.secondary_text_color
                    else
                        R.color.red_mnemonic
                )
            )

            txtCoinsWillBeSent.text =
                requireActivity().getStringResource(
                    if (it.namesDao.isNotEmpty())
                        R.string.coins_will_be_sent
                    else
                        R.string.address_doesnt_found
                )

            txtCoinsWillBeSentAddress.text = formatString(10, it.namesDao, 6)
        }
    }

    private fun generateLinearLayoutProperties(
        firstView: Boolean,
        key: String,
        value: String
    ): View {
        val linearLayout = LayoutInflater.from(getMainActivity())
            .inflate(R.layout.item_horizontal_nft_property, binding.containerProperties, false)
        val txtKey = linearLayout.findViewById<TextView>(R.id.edtPropertyKey)
        val txtValue = linearLayout.findViewById<TextView>(R.id.edtPropertyValue)
        txtKey.text = key
        txtValue.text = value
        txtKey.id = View.generateViewId()
        txtValue.id = View.generateViewId()
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        if (!firstView)
            layoutParams.marginStart = getMainActivity().convertDpToPixel(3)
        linearLayout.layoutParams = layoutParams
        return linearLayout
    }

    private fun copyToClipBoardShowCopied(text: String) {
        val clipBoard =
            getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "label",
            text
        )
        clipBoard.setPrimaryClip(clip)
        binding.relCopied.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(2000)
            kotlin.runCatching {
                binding.relCopied.visibility = View.GONE
            }.onFailure {
                VLog.d("Exception in changing relCopied visibility")
            }
        }
    }

    private fun showConfirmTransactionDialog() {
        val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_confirm_send_nft)
        registerBtnClicks(dialog)
        initConfirmDialogDetails(dialog)
        val width = resources.displayMetrics.widthPixels
        dialog.apply {
            addingDoubleDotsTxt(txtNftAddress)
            addingDoubleDotsTxt(txtNftCommission)
        }
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun registerBtnClicks(dialog: Dialog) {

        with(dialog) {

            findViewById<Button>(R.id.btnConfirm).setOnClickListener {
                dialog.dismiss()
                getMainActivity().showEnterPasswordFragment(reason = ReasonEnterCode.SEND_MONEY)
                waitingResponseEntPassCodeFragment()
            }

            findViewById<LinearLayout>(R.id.back_layout).setOnClickListener {
                dialog.dismiss()
            }

        }

    }

    private var passcodeJob: Job? = null

    private fun waitingResponseEntPassCodeFragment() {
        passcodeJob?.cancel()
        passcodeJob = lifecycleScope.launch {
            getMainActivity().mainViewModel.money_send_success.collect {
                if (it) {
                    VLog.d("Success entering the passcode : $it")
                    delay(500)
                    dialogManager.showProgress(getMainActivity())
                    var destAddress = binding.edtAddressWallet.text.toString()
                    vm.viewState.value.namesDao?.let { names ->
                        destAddress = names
                    }

                    initFlutterToGenerateSpendBundle(destAddress)
                    getMainActivity().mainViewModel.sendMoneyFalse()
                    pushTransactionState()
                }
            }
        }
    }

    private var genSpendBundleJob: Job? = null

    private val handler = CoroutineExceptionHandler { _, ex ->
        VLog.d("Exception caught on send  nft fragment : ${ex.message} ")
        dialogManager.hidePrevDialogs()
        showFailedSendingTransaction()
    }

    private fun initFlutterToGenerateSpendBundle(toAddress: String) {
        genSpendBundleJob?.cancel()
        genSpendBundleJob = lifecycleScope.launch(handler) {
            val methodChannel = MethodChannel(
                (getMainActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
                METHOD_CHANNEL_GENERATE_HASH
            )

            val argsFlut = hashMapOf<String, Any>()
            val wallet = vm.wallet ?: return@launch
            val nftCoin = vm.nftCoin ?: return@launch
            val alreadySpentCoins = vm.alreadySpentCoins
            val coin = CoinRecord(
                coin = com.green.wallet.data.network.dto.coins.Coin(
                    amount = 1,
                    parent_coin_info = nftCoin.coinInfo,
                    puzzle_hash = nftCoin.coinHash
                ),
                confirmed_block_index = nftCoin.confirmedBlockIndex,
                spent = false,
                spent_block_index = nftCoin.spentBlockIndex,
                timestamp = nftCoin.timeStamp,
                coinbase = false
            )
            val enteredFee = vm.viewState.value.fee
            VLog.d("Entered Fee NFT Send Fragment : $enteredFee")
            val fee = (Math.round(
                enteredFee * if (isThisChivesNetwork(wallet.networkType)) Math.pow(
                    10.0,
                    8.0
                ) else Math.pow(10.0, 12.0)
            ))
            val gson = Gson()
            argsFlut["observer"] = wallet.observerHash
            argsFlut["non_observer"] = wallet.nonObserverHash
            argsFlut["destAddress"] = toAddress
            argsFlut["mnemonics"] = convertListToStringWithSpace(wallet.mnemonics)
            argsFlut["coin"] = gson.toJson(coin)
            argsFlut["base_url"] = vm.baseUrl ?: return@launch
            argsFlut["spentCoins"] = Gson().toJson(alreadySpentCoins)
            argsFlut["fromAddress"] = nftCoin.puzzleHash
            argsFlut["fee"] = fee

            methodChannel.setMethodCallHandler { method, callBack ->
                if (method.method == "nftSpendBundle") {

                    VLog.d("SpendBundleJson for NFT from flutter : ${method.arguments}")
                    val argAnd = (method.arguments as HashMap<*, *>)
                    val spendBundleJson =
                        argAnd["spendBundle"].toString()
                    val spentCoinsJson =
                        argAnd["spentCoins"].toString()

                    vm.sendNFTBundle(
                        spendBundleJson,
                        nftCoin.puzzleHash,
                        spentCoinsJson,
                        nftInfo,
                        enteredFee,
                        nftCoin.confirmedBlockIndex.toInt(),
                        wallet.networkType
                    )

                } else if (method.method == "failedNFT") {
                    showFailedSendingTransaction()
                    Toast.makeText(
                        getMainActivity(),
                        "Exception : ${method.arguments}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            methodChannel.invokeMethod("generateNftSpendBundle", argsFlut)
        }
    }

    private fun showFailedSendingTransaction() {
        dialogManager.hidePrevDialogs()
        getMainActivity().apply {
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

    private fun initConfirmDialogDetails(dialog: Dialog) {
        dialog.apply {
            var commissionText = formattedDoubleAmountWithPrecision(vm.viewState.value.fee)
            if (commissionText.isEmpty())
                commissionText = "0"
            findViewById<TextView>(R.id.edtConfirmSendNftAddress).text =
                binding.edtAddressWallet.text.toString()
            findViewById<TextView>(R.id.edtCommission).text = commissionText
            findViewById<TextView>(R.id.edtNFTName).text = nftInfo.name
            findViewById<TextView>(R.id.edtNftCollection).text = nftInfo.collection
            findViewById<TextView>(R.id.edtNftID).text = formatString(10, nftInfo.nft_id, 4)
            Glide.with(getMainActivity()).load(nftInfo.data_url)
                .placeholder(getMainActivity().getDrawableResource(R.drawable.img_nft))
                .into(findViewById(R.id.img_nft))
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

    private fun getQrCodeDecoded() {
        prevEnterAddressJob?.cancel()
        prevEnterAddressJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    getMainActivity().mainViewModel.decodedQrCode.collectLatest {
                        if (it.isNotEmpty()) {
                            binding.edtAddressWallet.setText(it)
                            getMainActivity().mainViewModel.saveDecodeQrCode("")
                        }
                    }
                }
                getMainActivity().mainViewModel.chosenAddress.collectLatest { addres ->
                    VLog.d("Chosen Address from AddressFragment : $addres")
                    if (addres.isNotEmpty()) {
                        binding.edtAddressWallet.setText(addres)
                        getMainActivity().mainViewModel.saveChosenAddress("")
                    }
                }
            }
        }
    }

    override fun collectFlowOnStarted(scope: CoroutineScope) {
        vm.viewState.collectFlow(scope) {
            initButtonState(it)
            initAddressLookUp(it)
        }
    }

    private fun initButtonState(it: NftSendViewState) {
        binding.btnContinue.isEnabled = it.feeEnough && it.destAddress.isNotEmpty()
    }

    override fun onDestroyView() {
        getMainActivity().sendNftFragmentView = null
        super.onDestroyView()
    }

}
