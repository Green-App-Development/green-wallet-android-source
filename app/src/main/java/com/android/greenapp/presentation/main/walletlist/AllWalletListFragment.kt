package com.android.greenapp.presentation.main.walletlist


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
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentAllWalletListBinding
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.ReasonEnterCode
import com.android.greenapp.presentation.tools.getStringResource
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_all_wallet_list.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class AllWalletListFragment : DaggerFragment(),
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
	private var job: Job? = null
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

		job = lifecycleScope.launch {
			viewModel.getAllWalletListFirstHomeIsAddedThenRemain().collect {
				it.let {
					VLog.d("Incoming wallet list : $it")
					walletListAdapter.walletList(it)
				}
			}
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
			curActivity().showEnterPasswordFragment(ReasonEnterCode.DELETE_WALLET)
			listeningToEnteredPasscode(wallet)
		}

		dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
			dialog.dismiss()
		}

	}

	private fun listeningToEnteredPasscode(wallet: Wallet) {
		enterPasswordJob?.cancel()
		enterPasswordJob = lifecycleScope.launch {
			curActivity().mainViewModel.delete_wallet.collect {
				if (it) {
					VLog.d("Delete Wallet Confirmation Dialog called")
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
				} else {
					VLog.d("DeleteWallet became false")
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
		job?.cancel()
	}


}
