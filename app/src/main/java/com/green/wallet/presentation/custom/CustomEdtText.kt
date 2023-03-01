package com.green.wallet.presentation.custom

import android.content.Context
import android.util.AttributeSet
import com.green.wallet.R


class CustomEdtText(mContext: Context, attributeSet: AttributeSet) :
	androidx.appcompat.widget.AppCompatEditText(mContext, attributeSet) {

	private lateinit var listener: EdtListener

	override fun onTextContextMenuItem(id: Int): Boolean {
		if(!this::listener.isInitialized)
			return false
		when (id) {
			R.id.cut -> {

			}
			android.R.id.paste -> {
				listener.onPasteText()
			}
			android.R.id.copy -> {

			}
		}
		return false
	}

	fun initEdtListener(listener: EdtListener) {
		this.listener = listener
	}


	interface EdtListener {

		fun onPasteText();

	}


}
