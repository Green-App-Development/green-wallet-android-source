package com.green.wallet.presentation.main.walletlist


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.green.wallet.R
import com.green.wallet.databinding.FragmentAllWalletListBinding
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.custom.base.BaseFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.main.pincode.PinCodeFragment
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.ext.collectFlow
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_all_wallet_list.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class AllWalletListFragment : BaseFragment(),
    ItemWalletAdapter.DetailLayoutListener {

    private lateinit var binding: FragmentAllWalletListBinding


    lateinit var walletListAdapter: ItemWalletAdapter

    @Inject
    lateinit var dialogManager: DialogManager

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: WalletListViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var effect: AnimationManager

    private val handler = CoroutineExceptionHandler { _, ex ->
        VLog.d("Caught Exception from Coroutine Scope : ${ex.message}")
    }
    private var listJob: Job? = null
    private var enterPasswordJob: Job? = null
    private var networkTypesJob: Job? = null
    private var job5Seconds: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllWalletListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VLog.d("OnViewCreated on AllWalletListFragment")
        initWalletRecView()
        registerBtnClicks()

    }

    private fun registerBtnClicks() {

        binding.backLayout.setOnClickListener {
            curActivity().popBackStackOnce()
        }


    }

    private fun initWalletRecView() {
        walletListAdapter = ItemWalletAdapter(curActivity(), effect = effect, lifecycleScope)
        binding.recViewItems.apply {
            layoutManager = LinearLayoutManager(curActivity())
            adapter = walletListAdapter
        }
        walletListAdapter.settingDetailLayoutListener(this)
        listJob?.cancel()
        listJob = lifecycleScope.launch {
            val res = viewModel.getAllWalletListFirstHomeIsAddedThenRemain()
            walletListAdapter.walletList(res)
        }

    }

    private fun tempMethod(mnemonics: List<String>) {
        var str = ""
        for (k in mnemonics) {
            str = "$str , \"$k\""
        }
        VLog.d("Mnenonics String : $str")
    }


    private fun curActivity() = requireActivity() as MainActivity

    private fun makeViewGoneAfter5Seconds(v: RelativeLayout) {

        lifecycleScope.launch(handler) {
            delay(4000)
            v.visibility = View.GONE
        }
    }

    override fun walletHomeAdded(wallet: Wallet, imageView: ImageView, callBack: () -> Unit) {
        lifecycleScope.launch {
            val homeCounter = viewModel.getHomeAddCounter()
            val lastTimeAdded = wallet.home_id_added
            VLog.d("Adding wallet to home : $homeCounter and lastTimeAdded : $lastTimeAdded")
            if (lastTimeAdded == 0L && homeCounter >= 10) {
                VLog.d("home Counter is exceeding 10 or more")
                curActivity().apply {
                    dialogManager.showFailureDialog(
                        this,
                        getStringResource(R.string.pop_up_failed_error_title),
                        getStringResource(R.string.the_application_does_not_support_more_than_wallets),
                        getStringResource(R.string.return_btn)
                    ) {
                        callBack()
                    }
                }
                return@launch
            }

            if (lastTimeAdded != 0L && homeCounter == 1) {
                VLog.d("There should be at least one wallet added to home")
                return@launch
            }

            if (lastTimeAdded == 0L) {
                viewModel.increaseHomeAddCounter()
                viewModel.updateHomeIdAdded(System.currentTimeMillis(), wallet.fingerPrint)
                relAddedHomeScreen.visibility = View.VISIBLE
                makeViewGoneAfter5Seconds(relAddedHomeScreen)
            } else {
                viewModel.decreaseHomeAddCounter()
                viewModel.updateHomeIdAdded(0L, wallet.fingerPrint)
                relRemovedFromHome.visibility = View.VISIBLE
                makeViewGoneAfter5Seconds(relRemovedFromHome)
            }
            wallet.home_id_added =
                if (wallet.home_id_added == 0L) System.currentTimeMillis() else 0
            imageView.setImageResource(if (wallet.home_id_added > 0L) R.drawable.ic_star_enabled else R.drawable.ic_star_not_enabled)
            VLog.d("Home Wallet Added Counter : ${viewModel.getHomeAddCounter()}")
        }
    }

    override fun deleteClicked(wallet: Wallet) {
        showConfirmationDialog(wallet)
    }

    override fun onWalletItemClicked(itemId: Int) {
        curActivity().move2ManageWalletFragment(itemId)
    }

    override fun addWallet() {
        curActivity().showBtmDialogCreateOrImportNewWallet(true)
    }

    private fun showConfirmationDialog(wallet: Wallet) {
        val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
        dialog.setContentView(R.layout.dialog_confirm_delete_wallet)
        registerBtnClicks(dialog, wallet)
        val width = resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun registerBtnClicks(dialog: Dialog, wallet: Wallet) {

        dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            viewModel.wallet = wallet
            PinCodeFragment.build(reason = ReasonEnterCode.REMOVE_WALLET)
                .show(childFragmentManager, "")
        }

        dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun collectFlowOnStarted(scope: CoroutineScope) {
        viewModel.event.collectFlow(scope) {
            when (it) {
                ListWalletEvent.PinCodeConfirmToDelete -> {
                    curActivity().apply {
                        dialogManager.showSuccessDialog(
                            this,
                            getStringResource(R.string.pop_up_wallet_removed_title),
                            getStringResource(R.string.pop_up_wallet_removed_description),
                            getStringResource(R.string.ready_btn)
                        ) {

                        }
                    }
                    initWalletRecView()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listJob?.cancel()
    }


}
