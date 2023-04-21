package com.green.wallet.presentation.main.btmdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogChooseDAppsBinding
import com.green.wallet.presentation.main.MainActivity

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

			relItemMetaMask.setOnClickListener {

			}

			relItemTrust.setOnClickListener {

			}

			relPayAddress.setOnClickListener {

			}
		}
	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}

	private fun curActivity() = requireActivity() as MainActivity


}


