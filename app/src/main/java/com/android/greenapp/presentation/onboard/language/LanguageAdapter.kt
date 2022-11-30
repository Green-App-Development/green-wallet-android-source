package com.android.greenapp.presentation.onboard.language

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.data.network.dto.greenapp.lang.LanguageItemDto
import com.android.greenapp.presentation.tools.loadSvg

/**
 * Created by bekjan on 25.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class LanguageAdapter(
	private val languageClicker: LanguageClicker,
	private val activity: Activity
) :
	RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

	private val languageList = mutableListOf<LanguageItemDto>()

	fun updateLanguageList(list: List<LanguageItemDto>) {
		languageList.clear()
		languageList.addAll(list)
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
		return LanguageViewHolder(view, languageClicker)
	}

	override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
		holder.onBind(languageList[position])
	}

	override fun getItemCount() = languageList.size


	inner class LanguageViewHolder(v: View, val languageClicked: LanguageClicker) :
		RecyclerView.ViewHolder(v) {
		private val txtLanguage = v.findViewById<TextView>(R.id.txtLanguage)
		private val rootRelative = v.findViewById<RelativeLayout>(R.id.rootRelative)
		private val img_flag = v.findViewById<ImageView>(R.id.ic_flag)

		fun onBind(languageItem: LanguageItemDto) {
			rootRelative.setOnClickListener {
				languageClicked.onLanguageClicked(languageItem)
			}
			txtLanguage.text = languageItem.name_btn

			img_flag.loadSvg(languageItem.icon)
		}

	}

	interface LanguageClicker {
		fun onLanguageClicked(langItem: LanguageItemDto)
	}

}
