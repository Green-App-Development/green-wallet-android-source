package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogBtmCreateOfferBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class BtmCreateOfferDialog : BottomSheetDialogFragment() {

	private lateinit var binding: DialogBtmCreateOfferBinding

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
		val view = DialogBtmCreateOfferBinding.inflate(layoutInflater)
		return view.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On View Created Create Offer with vm : $vm")

	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}


}
