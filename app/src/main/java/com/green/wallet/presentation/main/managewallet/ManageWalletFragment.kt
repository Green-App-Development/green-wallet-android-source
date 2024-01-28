package com.green.wallet.presentation.main.managewallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.green.wallet.R
import com.green.wallet.databinding.FragmentManageWalletBetaBinding
import com.example.common.tools.*
import dagger.android.support.DaggerFragment
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.base.BaseFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.pincode.PinCodeFragment
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getStringResource
import com.greenwallet.core.ext.collectFlow
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_manage_wallet_beta.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class ManageWalletFragment : BaseFragment(),
    ManageWalletViewPagerAdapter.ManageWalletAdapterListener {

    private lateinit var binding: FragmentManageWalletBetaBinding

    lateinit var manageWalletAdapter: ManageWalletViewPagerAdapter

    @Inject
    lateinit var dialogManager: DialogManager

    @Inject
    lateinit var effect: AnimationManager

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ManageWalletViewModel by viewModels { viewModelFactory }


    companion object {
        const val WALLET_KEY = "wallet_key"
    }

    private var curChosenWalletPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            curChosenWalletPosition = it.getInt(WALLET_KEY)
            VLog.d("Parced Wallet Position for Adapter to Show : $curChosenWalletPosition")
        }
    }

    private var viewPagerJob: Job? = null
    private var enterPasswordJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        VLog.d("On create view on manage wallet")
        binding = FragmentManageWalletBetaBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VLog.d("On view crated view on manage wallet")
        initViewPagerWithAdapter()
        registerBtnClicks()
        curActivity().mainViewModel.show_data_wallet_invisible()
    }

    private var job30s: Job? = null


    override fun onStart() {
        super.onStart()
        VLog.d("On view start view on manage wallet")
    }

    override fun onResume() {
        super.onResume()
        VLog.d("On view resume view on manage wallet")
    }


    private fun registerBtnClicks() {

        binding.apply {
            backLayout.setOnClickListener {
                initBackButton()
            }

            linearSend.setOnClickListener {
                curActivity().move2SendFragment(
                    networkType = manageWalletAdapter.walletList[manageWalletViewPager.currentItem].networkType,
                    manageWalletAdapter.walletList[manageWalletViewPager.currentItem].fingerPrint,
                    true
                )
            }

            linearReceive.setOnClickListener {
                curActivity().move2ReceiveFragment(
                    manageWalletAdapter.walletList[manageWalletViewPager.currentItem].networkType,
                    manageWalletAdapter.walletList[manageWalletViewPager.currentItem].fingerPrint
                )
            }

            linearShare.setOnClickListener {
                sendWalletAddress()
            }

            linearScan.setOnClickListener {
                requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
            }

            txtDeleteWallet.setOnClickListener {
                showConfirmationDialog(manageWalletAdapter.walletList[manageWalletViewPager.currentItem])
            }

            btnStoryTransactions.setOnClickListener {
                val wallet = manageWalletAdapter.walletList[manageWalletViewPager.currentItem]
                curActivity().move2TransactionsFragment(wallet.fingerPrint, wallet.address)
            }

        }
    }

    private fun initBackButton() {
        curActivity().popBackStackOnce()
    }

    private fun sendWalletAddress() {
        val intent = Intent(Intent.ACTION_SEND)
        val content =
            manageWalletAdapter.walletList[binding.manageWalletViewPager.currentItem].address
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.address_wallet)
        )
        intent.putExtra(Intent.EXTRA_TEXT, content)
        requestSendingTextViaMessengers.launch(intent)
    }

    private fun initViewPagerWithAdapter() {
        viewPagerJob = lifecycleScope.launchWhenStarted {
            launch {
                viewModel.getFlowAllWalletListFirstHomeIsAddedThenRemain()
                    .collectLatest { walletList ->

                        if (walletList.isEmpty()) {
                            curActivity().move2HomeFragment()
                            return@collectLatest
                        }
                        VLog.d("FLow of wallet List changed on manage view : $walletList")
                        manageWalletAdapter = ManageWalletViewPagerAdapter(
                            effect = effect,
                            walletList = walletList,
                            adapterListener = this@ManageWalletFragment,
                            activity = this@ManageWalletFragment.curActivity()
                        )
                        binding.manageWalletViewPager.adapter = manageWalletAdapter
                        binding.pageIndicator.count = Math.min(walletList.size, 10)
                        binding.manageWalletViewPager.addOnPageChangeListener(object :
                            ViewPager.OnPageChangeListener {
                            override fun onPageScrolled(
                                position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int
                            ) {

                            }

                            override fun onPageSelected(position: Int) {
                                if (position <= 10)
                                    binding.pageIndicator.setSelected(position)
                                curActivity().mainViewModel.show_data_wallet_invisible()
                                manageWalletAdapter.changeToShowData(
                                    curChosenWalletPosition,
                                    show_data_visible = false
                                )
                                curChosenWalletPosition = position
                            }

                            override fun onPageScrollStateChanged(state: Int) {

                            }

                        })
                        updateViewsIfSavedBefore(manageWalletAdapter, walletList)
                        curChosenWalletPosition =
                            Math.min(curChosenWalletPosition, walletList.size - 1)
                        delay(10)
                        manage_wallet_view_pager.setCurrentItem(
                            curChosenWalletPosition
                        )
                        binding.pageIndicator.setSelected(curChosenWalletPosition)
                    }
                VLog.d("Cur Chosen Wallet Position on Manage: $curChosenWalletPosition")

            }

            curActivity().mainViewModel.show_data_wallet.collect {
                job30s?.cancel()
                if (this@ManageWalletFragment::manageWalletAdapter.isInitialized)
                    manageWalletAdapter.changeToShowData(
                        binding.manageWalletViewPager.currentItem,
                        it
                    )
                if (it) {
                    job30s = lifecycleScope.launch {
                        delay(30000)
                        curActivity().mainViewModel.show_data_wallet_invisible()
                    }
                }
            }
        }

    }

    private fun updateViewsIfSavedBefore(
        manageWalletAdapter: ManageWalletViewPagerAdapter,
        walletList: List<Wallet>
    ) {
        for (i in 0 until walletList.size) {
            if (manageWalletAdapter.views[i] != null) {
                manageWalletAdapter.initViewDetails(
                    manageWalletAdapter.views[i]!!,
                    walletList[i],
                    i
                )
            }
        }
    }

    private fun showConfirmationDialog(wallet: Wallet) {
        val btnCancel = {

        }

        val btnConfirm = {
            listeningToEnterPasscode(wallet)
            curActivity().showEnterPasswordFragment(reason = ReasonEnterCode.DELETE_WALLET)
        }
        dialogManager.showConfirmationDialog(
            curActivity(),
            curActivity().getString(R.string.delet_wallet_warning_title),
            curActivity().getString(R.string.delet_wallet_warning_description),
            btnConfirm,
            btnCancel
        )
    }

    private fun listeningToEnterPasscode(wallet: Wallet) {
        enterPasswordJob?.cancel()
        enterPasswordJob = lifecycleScope.launch {
            curActivity().mainViewModel.delete_wallet.collect {
                if (it) {
                    viewModel.deleteWallet(wallet)
                    curActivity().apply {
                        dialogManager.showSuccessDialog(
                            this,
                            getStringResource(R.string.pop_up_wallet_removed_title),
                            getStringResource(R.string.pop_up_wallet_removed_description),
                            getStringResource(R.string.ready_btn)
                        ) {

                        }
                    }
                    curActivity().mainViewModel.deleteWalletFalse()
                }
            }
        }
    }


    private fun curActivity() = requireActivity() as MainActivity

    override fun showDataClicked() {
        PinCodeFragment.build(reason = ReasonEnterCode.SHOW_DETAILS)
            .show(childFragmentManager, "")
    }

    override fun imgCopyClicked(data: String) {
        VLog.d("Copy icon clicked in manageWalletViewPager : $data")
        val clipBoard =
            curActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "label",
            data
        )
        clipBoard.setPrimaryClip(clip)
        binding.relCopied.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(5000)
            binding.relCopied.visibility = View.GONE
        }
    }

    override fun settingsClicked(fingerPrint: Long, address: String) {
        curActivity().move2WalletSettings(address)
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it ->
            it.entries.forEach {
                if (it.value) {
                    val curWallet =
                        manageWalletAdapter.walletList[manage_wallet_view_pager.currentItem]
                    curActivity().move2ScannerFragment(curWallet.fingerPrint, curWallet.networkType)
                } else
                    Toast.makeText(
                        curActivity(),
                        curActivity().getStringResource(R.string.camera_permission_missing),
                        Toast.LENGTH_SHORT
                    )
                        .show()
            }
        }

    private val requestSendingTextViaMessengers =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            curActivity().backToMainWalletFragment()
        }

    override fun collectFlowOnStarted(scope: CoroutineScope) {
        viewModel.event.collectFlow(scope) {
            when (it) {
                is ManageWalletEvent.ShowData -> {
                    if (this@ManageWalletFragment::manageWalletAdapter.isInitialized)
                        manageWalletAdapter.changeToShowData(
                            binding.manageWalletViewPager.currentItem,
                            it.visible
                        )
                    if (it.visible) {
                        job30s = lifecycleScope.launch {
                            delay(30000)
                            viewModel.handleEvent(ManageWalletEvent.ShowData(false))
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        VLog.d("On Pause on managewalletfragment")
    }

    override fun onStop() {
        super.onStop()
        VLog.d("On Stop on managewalletfragment")
        viewPagerJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        VLog.d("On Destroy on managewalletfragment")
    }


}
