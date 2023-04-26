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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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

			val address = ""
			val amount = 0.001
			relItemTrust.setOnClickListener {
				val deepLink =
					"https://link.trustwallet.com/send?coin=ethereum&address=$address&amount=$amount&referrer=com.green.wallet"
				navigateToOtherApp(deepLink)
			}
			relItemMetaMask.setOnClickListener {
				val deepLink =
					"https://metamask.app.link/send/transaction?to=$address&value=$amount"
				navigateToOtherApp(deepLink)
			}
			relPayAddress.setOnClickListener {
				curActivity().move2QRSendFragment()
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


