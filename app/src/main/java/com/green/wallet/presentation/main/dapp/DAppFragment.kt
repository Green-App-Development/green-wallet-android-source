package com.green.wallet.presentation.main.dapp

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.green.wallet.R
import com.green.wallet.databinding.FragmentDAppBinding
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.dialog_confirm_send_nft.*


class DAppFragment : DaggerFragment() {

    private lateinit var binding: FragmentDAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDAppBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerListeners()
        binding.initChoicesDEXNftMarkets()
        underlineTxt()
    }

    private fun underlineTxt() {
        val text =
            SpannableString(getMainActivity().getStringResource(R.string.listing_request_title))
        text.setSpan(UnderlineSpan(), 0, text.length, 0)
        binding.txtListingApplication.apply {
            setText(text)
            setOnClickListener {
                getMainActivity().move2ListingFragment()
            }
        }
    }


    fun FragmentDAppBinding.registerListeners() {

    }

    private var dexIsClicked = true

    private fun FragmentDAppBinding.initChoicesDEXNftMarkets() {
        txtClicked(txtDEX)
        txtUnClicked(txtNFtMarkets)
        txtNoDAppList.text = getMainActivity().getStringResource(R.string.nft_dex_soon_text)
        txtDEX.setOnClickListener {
            if (dexIsClicked) return@setOnClickListener
            dexIsClicked = !dexIsClicked
            if (dexIsClicked) {
                txtClicked(txtDEX)
                txtUnClicked(txtNFtMarkets)
                txtNoDAppList.text = getMainActivity().getStringResource(R.string.nft_dex_soon_text)
            } else {
                txtUnClicked(txtDEX)
                txtClicked(txtNFtMarkets)
            }
        }
        txtNFtMarkets.setOnClickListener {
            if (!dexIsClicked) return@setOnClickListener
            dexIsClicked = !dexIsClicked
            if (!dexIsClicked) {
                txtClicked(txtNFtMarkets)
                txtUnClicked(txtDEX)
                txtNoDAppList.text =
                    getMainActivity().getStringResource(R.string.nft_markets_soon_text)
            } else {
                txtClicked(txtDEX)
                txtUnClicked(txtNFtMarkets)
            }
        }
    }

    private fun txtClicked(txt: TextView?) {
        txt?.apply {
            background.setTint(getMainActivity().getColorResource(R.color.green))
            setTextColor(getMainActivity().getColorResource(R.color.white))
        }
    }

    private fun txtUnClicked(txt: TextView?) {
        txt?.apply {
            background.setTint(getMainActivity().getColorResource(R.color.bcg_sorting_txt_category))
            setTextColor(getMainActivity().getColorResource(R.color.sorting_txt_category))
        }
    }


}


