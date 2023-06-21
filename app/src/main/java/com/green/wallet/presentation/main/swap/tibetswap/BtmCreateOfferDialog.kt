package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout
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
        binding = DialogBtmCreateOfferBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VLog.d("On View Created Create Offer with vm : $vm")
        binding.clickedFeeOptions()

    }

    private fun DialogBtmCreateOfferBinding.clickedFeeOptions() {

        relChosenFee.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = relChosenFee.width
                changePositionsClick(width = width)
                val layoutParams = relChosenFee.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                relChosenFee.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }

    private fun DialogBtmCreateOfferBinding.changePositionsClick(width: Int) {

        txtLong.setOnClickListener {

        }

    }

    private fun changePositionsFeeCombinations(from: Int, to: Int) {

        val key = "$from|$to"
        when (key) {
            "1|2" -> {

            }

            "1|3" -> {

            }

            "2|1" -> {

            }

            "2|3" -> {

            }

            "3|1" -> {

            }

            "3|2" -> {

            }
        }

    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }


}
