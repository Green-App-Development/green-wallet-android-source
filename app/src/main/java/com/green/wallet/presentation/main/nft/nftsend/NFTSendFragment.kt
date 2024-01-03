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
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStateAtLeast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.common.tools.addingDoubleDotsTxt
import com.example.common.tools.formatString
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.data.network.dto.coinSolution.Coin
import com.green.wallet.data.network.dto.coinSolution.CoinSolution
import com.green.wallet.data.network.dto.coins.CoinRecord
import com.green.wallet.databinding.FragmentSendNftBinding
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.nft.nftdetail.NFTDetailsFragment
import com.green.wallet.presentation.tools.*
import com.green.wallet.presentation.tools.Resource.Companion.error
import dagger.android.support.DaggerFragment
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.dialog_confirm_send_nft.*
import kotlinx.android.synthetic.main.fragment_send.txtAddressAlredyExistWarning
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


class NFTSendFragment : DaggerFragment() {

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
    private var spendableAmount = 0.0

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
        getQrCodeDecoded()
        vm.initNFTCoin(nftInfo)
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
            enableBtnContinueTwoEdtsFilled()
        }


        edtEnterCommission.setOnFocusChangeListener { view, focus ->
            if (focus) {
                txtEnterCommission.visibility = View.VISIBLE
                txtSpendableBalanceCommission.visibility = View.VISIBLE
                edtEnterCommission.hint = ""
                view3.setBackgroundColor(getMainActivity().getColorResource(R.color.green))
                enterCommissionToken.apply {
                    setTextColor(getMainActivity().getColorResource(R.color.green))
                    text = "XCH"
                }
                txtRecommendedCommission.visibility = View.VISIBLE
            } else if (edtEnterCommission.text.toString().isEmpty()) {
                txtEnterCommission.visibility = View.INVISIBLE
                edtEnterCommission.hint =
                    getMainActivity().getString(R.string.send_token_commission_amount)
                enterCommissionToken.apply {
                    text = "-"
                    setTextColor(getMainActivity().getColorResource(R.color.txtShortNetworkType))
                }
                txtRecommendedCommission.visibility = View.INVISIBLE
                txtSpendableBalanceCommission.visibility = View.GONE
            }
            if (!focus) {
                view3.setBackgroundColor(getMainActivity().getColorResource(R.color.edt_divider))
            }
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

        edtEnterCommission.addTextChangedListener {
            enableBtnContinueTwoEdtsFilled()
        }

    }

    fun FragmentSendNftBinding.updateViews() {
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.spendableBalance.collectLatest {
                    val txtSpendableBalance =
                        getMainActivity().getStringResource(R.string.spendable_balance)
                    spendableAmount = it.toDouble()
                    binding.txtSpendableBalanceCommission.setText(
                        "$txtSpendableBalance: $it"
                    )
                }
            }
        }

    }

    private fun notEnoughAmountWarningTextFee() {
        binding.apply {
            edtEnterCommission.setTextColor(getMainActivity().getColorResource(R.color.red_mnemonic))
            txtEnterCommission.setTextColor(getMainActivity().getColorResource(R.color.red_mnemonic))
        }
    }

    private fun hideNotEnoughAmountWarningFee() {
        binding.apply {
            edtEnterCommission.setTextColor(getMainActivity().getColorResource(R.color.secondary_text_color))
            txtEnterCommission.setTextColor(getMainActivity().getColorResource(R.color.green))
        }
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
                    initFlutterToGenerateSpendBundle(binding.edtAddressWallet.text.toString())
                    getMainActivity().mainViewModel.send_money_false()
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
            val wallet = vm.wallet
            val nftCoin = vm.nftCoin
            val alreadySpentCoins = vm.alreadySpentCoins
            val coin = CoinRecord(
                coin = com.green.wallet.data.network.dto.coins.Coin(
                    amount = 1,
                    parent_coin_info = nftCoin.coin_info,
                    puzzle_hash = nftCoin.coin_hash
                ),
                confirmed_block_index = nftCoin.confirmed_block_index,
                spent = false,
                spent_block_index = nftCoin.spent_block_index,
                timestamp = nftCoin.time_stamp,
                coinbase = false
            )
            val enteredFee = binding.edtEnterCommission.text.toString().toDoubleOrNull() ?: 0.0
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
            argsFlut["base_url"] = vm.base_url
            argsFlut["spentCoins"] = Gson().toJson(alreadySpentCoins)
            argsFlut["fromAddress"] = nftCoin.puzzle_hash
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
                        nftCoin.puzzle_hash,
                        spentCoinsJson,
                        nftInfo,
                        enteredFee,
                        nftCoin.confirmed_block_index.toInt(),
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
            var commissionText = binding.edtEnterCommission.text.toString()
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


    private fun enableBtnContinueTwoEdtsFilled() {
        val amount = binding.edtEnterCommission.text.toString().toDoubleOrNull() ?: 0.0
        var enabled = true
        if (amount > spendableAmount) {
            notEnoughAmountWarningTextFee()
            enabled = false
        } else {
            hideNotEnoughAmountWarningFee()
        }
        VLog.d("After checking to send Amount : $amount Enabled : $enabled and Spendable : $spendableAmount")
        binding.btnContinue.isEnabled = twoEdtsFilled.size >= 1 && enabled
    }


    override fun onDestroyView() {
        getMainActivity().sendNftFragmentView = null
        super.onDestroyView()
    }

}
