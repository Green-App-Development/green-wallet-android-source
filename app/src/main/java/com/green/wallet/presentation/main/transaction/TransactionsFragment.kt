package com.green.wallet.presentation.main.transaction

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.common.tools.*
import com.green.compose.theme.GreenWalletTheme
import com.green.wallet.R
import com.green.wallet.databinding.DialogTranNftDetailsBinding
import com.green.wallet.databinding.FragmentTransactionsBinding
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.custom.base.BaseFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.transaction.btmSpeedy.SpeedyBtmDialog
import com.green.wallet.presentation.tools.*
import com.green.wallet.presentation.viewBinding
import com.greenwallet.core.ext.collectFlow
import kotlinx.android.synthetic.main.fragment_transactions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class TransactionsFragment : BaseFragment(), TransactionItemAdapter.TransactionListener {


    companion object {
        const val FINGER_PRINT_KEY = "finger_print_key"
        const val ADDRESS_KEY = "address_key"
    }

    @Inject
    lateinit var animManager: AnimationManager

    private lateinit var dateAdapter: DynamicSpinnerAdapter

    private lateinit var networkAdapter: DynamicSpinnerAdapter

    private val binding by viewBinding(FragmentTransactionsBinding::bind)

    private lateinit var transactionItemAdapter: TransactionItemAdapter
    private lateinit var transAdapterPaging: TransPagingAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: TransactionsViewModel by viewModels { viewModelFactory }


    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    private var prevClickedStatus: TextView? = null

    private var updateTransJob: Job? = null

    @Inject
    lateinit var dialogManager: DialogManager

    //XCC  chives
    //XCH  chia
    private var currentFingerPrint: Long? = null
    private var currentAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val incoming = it.getLong(FINGER_PRINT_KEY)
            currentFingerPrint = if (incoming == 0L) null else incoming
            curActivity().shouldGoBackHomeFragmentFromTransactions = currentFingerPrint == null
            val address = it.getString(ADDRESS_KEY, "")
            currentAddress = if (address == "") null else address
        }
        VLog.d("Got Incoming arguments For Transactions Fragment : $currentFingerPrint ")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerClicks()
        initDateAdapter()
        initNetworkTypeAdapter()
        initStatusHorizontalView()
        initSortingByStatusClicks()
//		initTransactionItemAdapter()
        sortingByHeightAndSum()
        initSwipeRefreshLayout()
        addEmptyTransPlaceHolder()
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.apply {
            setOnRefreshListener {
                if (connectionLiveData.isOnline) {
                    viewModel.swipedRefreshClicked {
                        if (this@TransactionsFragment.isVisible) {
                            isRefreshing = false
                        }
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

    private fun updateTransactions() {
//		updateTransJob = lifecycleScope.launch {
//			viewModel.getAllQueriedFlowTransactionList(
//				currentAddress,
//				getCurSearchAmount(),
//				getCurChosenNetworkType(),
//				getCurChosenStatus(),
//				getCurSearchTransByDateCreatedExceptYesterday(),
//				getCurSearchByForYesterdayStart(),
//				getCurSearchByForYesterdayEnds(),
//				getTokenCode()
//			).collectLatest { transList ->
//				binding.apply {
//					if (transList.isNotEmpty()) {
//						recTransactionItems.visibility = View.VISIBLE
//						txtNoTrans.visibility = View.GONE
//						updateTransactionItems(transList)
//					} else {
//						recTransactionItems.visibility = View.GONE
//						txtNoTrans.visibility = View.VISIBLE
//					}
//				}
//			}
//		}

        viewModel.getAllQueriedFlowTransactionList(
            currentAddress,
            getCurSearchAmount(),
            getCurChosenNetworkType(),
            getCurChosenStatus(),
            getCurSearchTransByDateCreatedExceptYesterday(),
            getCurSearchByForYesterdayStart(),
            getCurSearchByForYesterdayEnds(),
            getTokenCode()
        )

//            transAdapterPaging.loadStateFlow.collectLatest {
//                if (it.append is LoadState.NotLoading && it.append.endOfPaginationReached) {
//
//                }
//            }
    }

    private fun getTokenCode(): String? {
        val searchText = edtSearchTrans.text.toString()
        if (searchText.toDoubleOrNull() != null)
            return null
        if (searchText.isEmpty())
            return null
        return searchText
    }

    private fun initStatusHorizontalView() {
        val display =
            (requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
        val threeDivided = display / 3 - 15
        for (txt in binding.linearSortingByStatus.children) {
            if (txt is TextView)
                txt.layoutParams.width = threeDivided
        }
    }


    private fun sortingByHeightAndSum() {
        binding.edtSearchTrans.addTextChangedListener {
            updateTransactions()
        }
    }

    private suspend fun updateTransactionItems(transList: List<TransferTransaction>) {
        delay(50)
        val recHeight = binding.recTransactionItems.height
        val dp = curActivity().pxToDp(recHeight)
        val atLeastItemCount = (dp / 45) - 2
        VLog.d("At Least Item Count of RecyclerView Items : $atLeastItemCount")
        transactionItemAdapter.itemCountFitsScreen = atLeastItemCount
        transactionItemAdapter.updateTransactionList(transList)
        transactionItemAdapter.notifyDataSetChanged()
    }

    private fun addEmptyTransPlaceHolder() {
        lifecycleScope.launch {
            delay(50)
            val recHeight = binding.placeHolderLinearView.height
            val dp = curActivity().pxToDp(recHeight)
            val atLeastItemCount = (dp / 45) - 3
            VLog.d("Number of empty item to add is : $atLeastItemCount")
            repeat(atLeastItemCount) {
                val emptyView =
                    layoutInflater.inflate(R.layout.empty_tran_view_place_holder, null, false)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getMainActivity().convertDpToPixel(45)
                )
                emptyView.layoutParams = layoutParams
                binding.placeHolderLinearView.addView(emptyView)
            }
        }
    }


    private fun initSortingByStatusClicks() {
        for (txt in binding.linearSortingByStatus.children) {
            if (txt is TextView) {
                sortingCategoryUnClicked(txt)
                txt.setOnClickListener {
                    if (it == prevClickedStatus)
                        return@setOnClickListener
                    sortingCategoryUnClicked(prevClickedStatus)
                    sortingCategoryClicked(txt)
                    prevClickedStatus = txt
                    updateTransactions()
                }
            }
        }
        sortingCategoryClicked(txtSortingAll)
        prevClickedStatus = txtSortingAll
    }


    private fun sortingCategoryClicked(txt: TextView?) {
        txt?.apply {
            background.setTint(curActivity().getColorResource(R.color.green))
            setTextColor(curActivity().getColorResource(R.color.white))
        }
    }

    private fun sortingCategoryUnClicked(txt: TextView?) {
        txt?.apply {
            background.setTint(curActivity().getColorResource(R.color.bcg_sorting_txt_category))
            setTextColor(curActivity().getColorResource(R.color.sorting_txt_category))
        }
    }

    private fun initNetworkTypeAdapter() {
        lifecycleScope.launch {

            val distinctNetworkTypes =
                viewModel.getDistinctNetworkTypeValues().toMutableList()

            distinctNetworkTypes.add(0, curActivity().getStringResource(R.string.transactions_all))

            networkAdapter =
                DynamicSpinnerAdapter(
                    180,
                    curActivity(),
                    distinctNetworkTypes
                )
            binding.networkSpinnerTrans.adapter = networkAdapter

            binding.networkSpinnerTrans.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        networkAdapter.selectedPosition = p2
                        updateTransactions()
                        updateCurChosenNetworkTypeTxt(distinctNetworkTypes[p2], p2)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }
        }
    }

    private fun updateCurChosenNetworkTypeTxt(network: String, position: Int) {
//		var len: Int
//		if (position == 0)
//			len = 145
//		else if (network.contains("Chia"))
//			len = 350
//		else
//			len = 400
//
//		rel_txt_network.layoutParams.width = len
        binding.txtNetworkType.text = network
    }

    private fun getCurChosenStatus(): Status? {
        if (prevClickedStatus == null || prevClickedStatus!!.tag == "txtSortingAll") return null
        return when (prevClickedStatus!!.tag.toString()) {
            Status.Incoming.toString() -> {
                Status.Incoming
            }

            Status.Outgoing.toString() -> {
                Status.Outgoing
            }

            Status.InProgress.toString() -> {
                Status.InProgress
            }

            else -> {
                null
            }
        }
    }

    private fun getCurSearchAmount(): Double? {
        if (edtSearchTrans.text.toString().toDoubleOrNull() == null) return null
        return edtSearchTrans.text.toString().toDouble()
    }

    private fun getCurSearchTransByDateCreatedExceptYesterday(): Long? {
        VLog.d("Date Position Selected : ${dateAdapter.selectedPosition}")
        return when (dateAdapter.selectedPosition) {
            0 -> null
            1 -> calculateMillisecondsPassedSinceMidNight(System.currentTimeMillis())
            2 -> null
            3 -> calculateMillisecondsPassedSinceLastWeek(System.currentTimeMillis())
            4 -> calculateMillisecondsPassedSinceLastMonth(System.currentTimeMillis())
            else -> null
        }
    }

    private fun getCurSearchByForYesterdayStart(): Long? {
        if (dateAdapter.selectedPosition == 2) {
            return calculateMillisecondsPassedSinceYesterday(System.currentTimeMillis())
        }
        return null
    }

    private fun getCurSearchByForYesterdayEnds(): Long? {
        if (dateAdapter.selectedPosition == 2) {
            return calculateMillisecondsPassedSinceMidNight(System.currentTimeMillis())
        }
        return null
    }

    private fun getCurChosenNetworkType(): String? {
        if (!this::networkAdapter.isInitialized) return null
        if (networkAdapter.selectedPosition == 0)
            return null

        return networkAdapter.dataOptions[networkAdapter.selectedPosition]
    }


    private fun initDateAdapter() {
        curActivity().apply {
            dateAdapter = DynamicSpinnerAdapter(
                180,
                curActivity(),
                mutableListOf(
                    getStringResource(R.string.transactions_all),
                    getStringResource(R.string.transactions_today),
                    getStringResource(R.string.transactions_yesterday),
                    getStringResource(R.string.transactions_last_week),
                    getStringResource(R.string.transactions_last_month)
                )
            )
        }
        binding.dateSpinner.adapter = dateAdapter

        binding.dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                dateAdapter.selectedPosition = p2
                updateTransactions()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.dateSpinner.setSpinnerEventsListener(object :
            CustomSpinner.OnSpinnerEventsListener {
            override fun onSpinnerOpened(spin: Spinner?) {
                binding.imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_enabled))
            }

            override fun onSpinnerClosed(spin: Spinner?) {
                binding.imgFilter.setImageDrawable(curActivity().getDrawableResource(R.drawable.ic_filter_reck_not_enabled))
            }
        })

    }

    private fun registerClicks() {

        binding.apply {

            imgFilter.setOnClickListener {
                it.startAnimation(animManager.getBtnEffectAnimation())
                binding.dateSpinner.performClick()
            }

            relTxtNetwork.setOnClickListener {
                binding.networkSpinnerTrans.performClick()
            }


        }

        animManager.animateArrowIconCustomSpinner(network_spinner_trans, imgArrow, curActivity())

        binding.backLayout.setOnClickListener {
            getMainActivity().popBackStackOnce()
        }

    }

    private fun showTransactionDetails(transaction: TransferTransaction) {
        if (transaction.code == "NFT") {
            showTransactionsNFTDetails(transaction)
        } else {
            val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
            dialog.setContentView(R.layout.dialog_transaction_details)
            val width = resources.displayMetrics.widthPixels
            dialog.apply {
                addingDoubleDotsTxt(findViewById(R.id.txtDate))
                addingDoubleDotsTxt(findViewById(R.id.txtCountCoins))
                addingDoubleDotsTxt(findViewById(R.id.txtCommission))
                addingDoubleDotsTxt(findViewById(R.id.txtHeightBlocks))
                findViewById<LinearLayout>(R.id.back_layout).setOnClickListener {
                    dialog.dismiss()
                }
            }
            initTransDetails(dialog, transaction)
            dialog.window?.setLayout(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
        }
    }

    private fun showTransactionsNFTDetails(transaction: TransferTransaction) {
        val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
        val binding = DialogTranNftDetailsBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        val width = resources.displayMetrics.widthPixels
        binding.apply {
            addingDoubleDotsTxt(txtNFTDate)
            addingDoubleDotsTxt(txtNftCommission)
            addingDoubleDotsTxt(txtNFTBlockHeight)
            backLayout.setOnClickListener {
                dialog.dismiss()
            }
        }
        initTransDetailsNFT(binding, transaction)
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun initTransDetailsNFT(
        binding: DialogTranNftDetailsBinding,
        transaction: TransferTransaction
    ) {
        binding.apply {
            val formattedDate = formattedDateForTransaction(
                curActivity(),
                transaction.createdAtTime
            )

            VLog.d("Formatted date for tran details : $formattedDate")
            binding.apply {
                edtNFTDate.text = formattedDate
                edtCommission.text =
                    "${formattedDoubleAmountWithPrecision(transaction.feeAmount)} ${
                        getShortNetworkType(
                            transaction.networkType
                        )
                    }"
                edtNFTBlockHeight.text = transaction.confirmedAtHeight.toString()

                viewModel.initNFTInfoByHash(transaction.nftCoinHash)
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.nftInfoState.collectLatest {
                            it?.let { nft ->
                                edtNFTName.text = nft.name
                                edtNftCollection.text = nft.collection
                                edtNftID.text = formatString(10, nft.nft_id, 4)
                                Glide.with(getMainActivity()).load(nft.data_url)
                                    .placeholder(getMainActivity().getDrawableResource(R.drawable.img_nft))
                                    .into(imgNft)
                            }
                        }
                    }
                }

            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun initTransDetails(dialog: Dialog, transaction: TransferTransaction) {
        VLog.d("Current Transaction Clicked -> : $transaction")
        dialog.apply {

            val formattedDate = formattedDateForTransaction(
                curActivity(),
                transaction.createdAtTime
            )

            VLog.d("Formatted date for tran details : $formattedDate")

            findViewById<TextView>(R.id.edtDate).text = formattedDate

            findViewById<TextView>(R.id.edtAmountTrans).text =
                "${formattedDoubleAmountWithPrecision(transaction.amount)} ${
                    transaction.code
                }"
            findViewById<TextView>(R.id.edtCommission).text =
                "${formattedDoubleAmountWithPrecision(transaction.feeAmount)} ${
                    getShortNetworkType(
                        transaction.networkType
                    )
                }"
            findViewById<TextView>(R.id.edtHeightBlocks).text = "${transaction.confirmedAtHeight}"
        }
    }

    override fun onResume() {
        super.onResume()
        updateTransactions()
    }

    override fun onStop() {
        super.onStop()
        updateTransJob?.cancel()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        VLog.d("TransactionFragment On DestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun curActivity() = requireActivity() as MainActivity

    override fun onTransactionItemClicked(transaction: TransferTransaction) {
        showTransactionDetails(transaction)
    }

    override fun onTransactionSpeedUpClick(transaction: TransferTransaction) {
        viewModel.handleIntent(TransactionIntent.OnSpeedyTran(transaction))
    }

    override fun onTransactionDelete(transaction: TransferTransaction) {
        viewModel.handleIntent(TransactionIntent.OnDeleteTransaction(transaction))
    }

    override fun collectFlowOnCreated(scope: CoroutineScope) {
        viewModel.event.collectFlow(scope) {
            when (it) {
                is TransactionEvent.SpeedyBtmDialog -> {
                    SpeedyBtmDialog.build(it.transaction).show(childFragmentManager, "")
                }

                is TransactionEvent.ShowWarningDeletionDialog -> {
                    VLog.d("Show Warning Deletion dialog got called")
                    dialogManager.showWarningDeleteTransaction(
                        requireActivity(),
                        "Warning!",
                        "This action will unlock the coins, but will not remove the transaction from the mempool." +
                                " If you are not sure of your actions, use the Speed up function",
                        "Cancel",
                        "Delete",
                        onDelete = {
                            viewModel.handleIntent(TransactionIntent.DeleteTransaction(it.transaction))
                        }
                    )
                }

                is TransactionEvent.ShowTransactionDetails -> {
                    showTransactionDetails(it.transaction)
                }
            }
        }
    }

    override fun collectFlowOnStarted(scope: CoroutineScope) {
        viewModel.viewState.collectFlow(scope) {
            initTransactionList(it)
        }
    }

    private fun initTransactionList(it: TransactionState) {
        binding.recTransactionItems.setContent {
            GreenWalletTheme {
                TransactionListScreen(
                    state = it,
                    onIntent = viewModel::handleIntent
                )
            }
        }
        isTransactionListEmpty(it.transactionList.isEmpty())
    }

    private fun isTransactionListEmpty(isEmpty: Boolean) {
        binding.apply {
            if (!isEmpty) {
                recTransactionItems.visibility = View.VISIBLE
                txtNoTrans.visibility = View.GONE
                placeHolderLinearView.visibility = View.VISIBLE
            } else {
                recTransactionItems.visibility = View.GONE
                txtNoTrans.visibility = View.VISIBLE
                placeHolderLinearView.visibility = View.VISIBLE
            }
        }
    }

}
