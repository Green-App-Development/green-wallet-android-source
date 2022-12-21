package com.green.wallet.presentation.main.importtoken

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.green.wallet.domain.domainmodel.Token
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class TokenAdapter(
	private val tokenListener: TokenAdapterListener,
	private val activity: Activity
) :
	RecyclerView.Adapter<TokenAdapter.TokenViewHolder>() {

	private val tokens = mutableListOf<Token>()

	var searching = false

	fun updateTokenList(list: List<Token>) {
		tokens.clear()
		tokens.addAll(list)
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_token, parent, false)
		return TokenViewHolder(view)
	}

	override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
		holder.homeSwitch.setOnCheckedChangeListener(null)
		holder.homeSwitch.isEnabled = true
		holder.homeSwitch.isChecked = false
		holder.onBind(tokens[position])
	}

	override fun getItemCount(): Int {
		return tokens.size
	}


	inner class TokenViewHolder(v: View) : RecyclerView.ViewHolder(v) {
		private val imgToken = v.findViewById<CircleImageView>(R.id.img_token)
		private val nameToken = v.findViewById<TextView>(R.id.txtTokenName)
		private val txtTokenCode = v.findViewById<TextView>(R.id.txtTokenCode)
		val homeSwitch = v.findViewById<SwitchCompat>(R.id.btn_switch_enabled_token)

		fun onBind(token: Token) {

			if (token.defaultTail == 1) {
				homeSwitch.isChecked = true
				homeSwitch.isEnabled = false
			}

			homeSwitch.isChecked = token.imported
			homeSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
				override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
					if (!searching) {
						tokenListener.importToken(p1, homeSwitch, token)
					}
				}
			})

			nameToken.text = token.name
			txtTokenCode.text = token.code

			Glide.with(activity).load(token.logo_url).placeholder(R.drawable.ic_circle_edge_token)
				.into(imgToken)

		}

	}

	interface TokenAdapterListener {
		fun importToken(added: Boolean, switchImport: SwitchCompat, token: Token)
	}


}
