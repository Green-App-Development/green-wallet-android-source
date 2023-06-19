package com.green.wallet.presentation.main.btmdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogBtmChooseWalletBinding
import com.green.wallet.databinding.ItemWalletBtmChooseBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.tibetswap.TibetSwapViewModel
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class BtmChooseWallet : BottomSheetDialogFragment() {

	private lateinit var binding: DialogBtmChooseWalletBinding

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetSwapViewModel by viewModels { viewModelFactory }


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(requireActivity().application as App).appComponent.inject(this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogBtmChooseWalletBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.initFingerPrints()

	}

	private fun DialogBtmChooseWalletBinding.initFingerPrints() {

		for (i in 123456 until 123460) {
			val bindingWallet = ItemWalletBtmChooseBinding.inflate(layoutInflater)
			VLog.d("Inflating view hash code  : ${view.hashCode()}")
			bindingWallet.
			containerWallet.addView(bindingWallet.root)
		}

	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}

}
