package extralogic.wallet.greenapp.presentation.custom

import android.content.Context
import android.util.AttributeSet
import com.android.greenapp.R

/**
 * Created by bekjan on 14.10.2022.
 * email: bekjan.omirzak98@gmail.com
 */
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
