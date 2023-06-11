package com.green.wallet.presentation.main.btmdialogs

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogChooseDAppsBinding
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getMainActivity

class BtmChooseDAppsPayment : BottomSheetDialogFragment() {

	lateinit var binding: DialogChooseDAppsBinding

	companion object {
		const val ADDRESS_KEY = "address_key"
		const val AMOUNT_KEY = "amount_key"
	}

	private var address: String = ""
	private var amount: Double = 0.0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			address = it.getString(ADDRESS_KEY, "")
			amount = it.getDouble(AMOUNT_KEY, 0.0)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogChooseDAppsBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerClicks()
	}

	private fun registerClicks() {
		binding.apply {
			relItemTrust.setOnClickListener {
				val deepLink =
					"https://link.trustwallet.com/send?asset=c195_tTR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t&address=$address&amount=$amount&memo=GreenWallet"
				navigateToOtherApp(deepLink)
			}
			relItemMetaMask.setOnClickListener {
				val deepLink =
					"https://metamask.app.link/send/transaction?to=$address&value=$amount"
				navigateToOtherApp(deepLink)
			}
			relPayAddress.setOnClickListener {
				curActivity().move2QRSendFragment(address)
			}
		}
	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}

	private fun curActivity() = requireActivity() as MainActivity

	private fun navigateToOtherApp(deepLink: String) {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
		startActivity(intent)
	}

}


