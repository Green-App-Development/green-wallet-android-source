package com.android.greenapp.presentation.main.managewallet

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
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentManageWalletBetaBinding
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.ReasonEnterCode
import com.android.greenapp.presentation.tools.getStringResource
import com.example.common.tools.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_manage_wallet_beta.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 28.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ManageWalletFragment : DaggerFragment(),
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
        binding = FragmentManageWalletBetaBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPagerWithAdapter()
        registerBtnClicks()
        curActivity().mainViewModel.show_data_wallet_invisible()
    }

    private var job30s: Job? = null


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
                curActivity().move2TransactionsFragment(manageWalletAdapter.walletList[manageWalletViewPager.currentItem].fingerPrint)
            }

        }
    }

    private fun initBackButton() {
//        val detailsShown = curActivity().mainViewModel.show_data_wallet.value
//        if (detailsShown) {
//            curActivity().mainViewModel.show_data_wallet_invisible()
//            return
//        }
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
        viewPagerJob?.cancel()
        viewPagerJob = lifecycleScope.launchWhenStarted {
            val walletList = viewModel.getAllWalletListFirstHomeIsAddedThenRemain()

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
                    curActivity().mainViewModel.show_data_wallet_invisible()
                    if (position <= 10)
                        binding.pageIndicator.setSelected(position)
                    manageWalletAdapter.changeToShowData(
                        curChosenWalletPosition,
                        show_data_visible = false
                    )
                    curChosenWalletPosition = position
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })

            curChosenWalletPosition = Math.min(curChosenWalletPosition, walletList.size - 1)
            manage_wallet_view_pager.setCurrentItem(
                curChosenWalletPosition
            )

            curActivity().mainViewModel.show_data_wallet.collect {
                job30s?.cancel()
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

//        manage_wallet_view_pager.postDelayed({
//            manage_wallet_view_pager.setCurrentItem(
//                curChosenWalletPosition
//            )
//        }, 100)

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
                            initViewPagerWithAdapter()
                        }
                    }
                    curActivity().mainViewModel.deleteWalletFalse()
                }
            }
        }
    }


    private fun curActivity() = requireActivity() as MainActivity

    override fun showDataClicked() {
        curActivity().showEnterPasswordFragment(ReasonEnterCode.SHOW_DATA)
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


}
