package com.green.wallet.presentation.main.home

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.TokenWallet
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.custom.isThisChivesNetwork
import com.bumptech.glide.Glide
import com.green.wallet.presentation.tools.getDrawableResource
import com.green.wallet.presentation.tools.getStringResource
import de.hdodenhof.circleimageview.CircleImageView



class WalletTokenAdapter(
	private val walletList: List<TokenWallet>,
	private val tokenClicker: TokenClicker,
	private val activity: Activity,
	private val balanceIsHidden: Boolean
) :
	RecyclerView.Adapter<RecyclerView.ViewHolder>() {


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == 0) {
			val view = LayoutInflater.from(parent.context)
				.inflate(R.layout.item_token_view_pager, parent, false)
			TokenViewHolder((view))
		} else {
			val view = LayoutInflater.from(parent.context)
				.inflate(R.layout.item_import_token_view_pager, parent, false)
			ImportTokenViewHolder((view))
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is TokenViewHolder) {
			holder.onBind(walletList[position])
		} else if (holder is ImportTokenViewHolder) {
			holder.onBind(walletList[0])
		}
	}

	override fun getItemCount() =
		walletList.size + 1


	override fun getItemViewType(position: Int): Int {
		return if (position < walletList.size) 0 else 1
	}

	@SuppressLint("SetTextI18n")
	private fun adjustTxtImport(v: View, activity: Activity, tokenName: String) {
		val import =
			if (isThisChivesNetwork(tokenName)) activity.getStringResource(R.string.import_mnemonics_soon) else activity.getStringResource(
				R.string.main_screen_purse_import
			)
		val txtImport = v.findViewById<TextView>(R.id.txtImportToken)
		txtImport.text =
			"    +   $import" + " ".repeat(
				7
			)
	}


	inner class TokenViewHolder(v: View) : RecyclerView.ViewHolder(v) {

		private val txtWalletName = v.findViewById<TextView>(R.id.txtNameWallet)
		private val txtWalletAmount = v.findViewById<TextView>(R.id.txtAmountWallet)
		private val txtAmountInUsd = v.findViewById<TextView>(R.id.txtAmountInUsd)
		private val imgToken = v.findViewById<CircleImageView>(R.id.img_token)


		@SuppressLint("SetTextI18n")
		fun onBind(token: TokenWallet) {

			txtWalletName.setText(token.name)
			initRegulateBalance(token)

			if (token.name.contains("Chia") && token.logo_ur.isEmpty()) {
				imgToken.setImageDrawable(activity.getDrawableResource(R.drawable.ic_chia_white))
			} else if (token.name.contains("Chives") && token.logo_ur.isEmpty()) {
				imgToken.setImageDrawable(activity.getDrawableResource(R.drawable.ic_chives_white))
			} else {
				Glide.with(activity).load(token.logo_ur).placeholder(R.drawable.white_radius)
					.into(imgToken)
			}

		}

		@SuppressLint("SetTextI18n")
		private fun initRegulateBalance(token: TokenWallet) {
			if (!balanceIsHidden) {
				txtWalletAmount.setText(
					"${formattedDoubleAmountWithPrecision(token.amount)} ${
						token.code
					}"
				)

				txtAmountInUsd.setText("⁓${formattedDollarWithPrecision(token.amountInUSD)} USD")
			} else {
				txtWalletAmount.setText("**** ${token.code}")
				txtAmountInUsd.setText("**** USD")
			}
		}

	}


	inner class ImportTokenViewHolder(v: View) : RecyclerView.ViewHolder(v) {

		private val rootImportToken = v.findViewById<RelativeLayout>(R.id.rel_import_token)
		private val importToken = v.findViewById<TextView>(R.id.txtImportToken)

		@SuppressLint("SetTextI18n")
		fun onBind(token: TokenWallet) {
			adjustTxtImport(importToken, activity, token.name)
			if (!isThisChivesNetwork(token.name)) {
				rootImportToken.setOnClickListener {
					tokenClicker.onImportTokenClicked()
				}
			}

		}

	}

	interface TokenClicker {
		fun onImportTokenClicked()
	}

}
