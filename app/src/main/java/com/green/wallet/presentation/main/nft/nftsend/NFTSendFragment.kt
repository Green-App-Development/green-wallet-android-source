package com.green.wallet.presentation.main.nft.nftsend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.tools.VLog
import com.example.common.tools.addingDoubleDotsTxt
import com.green.wallet.R
import com.green.wallet.databinding.FragmentSendNftBinding
import com.green.wallet.presentation.custom.getShortNetworkType
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.dialog_confirm_send_nft.*
import kotlinx.android.synthetic.main.dialog_confirm_transactions_coins.*
import kotlinx.android.synthetic.main.dialog_confirm_transactions_coins.txtAddress
import kotlinx.android.synthetic.main.dialog_confirm_transactions_coins.txtBlockChain
import kotlinx.android.synthetic.main.fragment_listing.*
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by bekjan on 08.02.2023.
 * email: bekjan.omirzak98@gmail.com
 */
class NFTSendFragment : DaggerFragment() {

	private lateinit var binding: FragmentSendNftBinding

	private val twoEdtsFilled = mutableSetOf<Int>()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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
		getQrCodeDecoded()
	}

	fun FragmentSendNftBinding.registerClicks() {

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
					text = getShortNetworkType("XCH")
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



	}

	private fun initConfirmDialogDetails(dialog: Dialog) {
		dialog.apply {
			var commissionText = binding.edtEnterCommission.text.toString()
			if (commissionText.isEmpty())
				commissionText = "0"
			findViewById<TextView>(R.id.edtConfirmSendNftAddress).text = binding.edtAddressWallet.text.toString()
			findViewById<TextView>(R.id.edtCommission).text = commissionText
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
		binding.btnContinue.isEnabled = twoEdtsFilled.size >= 1
	}


	override fun onDestroyView() {
		getMainActivity().sendNftFragmentView = null
		super.onDestroyView()
	}

}
