package com.green.wallet.presentation.main.btmdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.green.wallet.R
import com.green.wallet.presentation.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BtmSheetDialogNewOrImport : BottomSheetDialogFragment() {

    lateinit var networkType: String

    companion object{
        var NETWORK_TYPE_KEY="network_type_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            networkType=it.getString(NETWORK_TYPE_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RelativeLayout>(R.id.rel_new_wallet)?.setOnClickListener {
            curActivity().move2CoinsDetailsFragment(networkType)
        }
        view.findViewById<RelativeLayout>(R.id.rel_import)?.setOnClickListener {
            curActivity().move2ImportMnemonicFragment(networkType)
        }
    }

    private fun curActivity()=requireActivity() as MainActivity


    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }



}
