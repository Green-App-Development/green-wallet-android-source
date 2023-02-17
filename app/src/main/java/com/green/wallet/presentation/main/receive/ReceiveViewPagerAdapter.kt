package com.green.wallet.presentation.main.receive

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.tools.VLog

/**
 * Created by bekjan on 17.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ReceiveViewPagerAdapter(private val walletList: List<Wallet>, private val context: Context) :
	PagerAdapter() {

	private val views = Array<View?>(walletList.size) { null }

	override fun getCount(): Int {
		return walletList.size
	}

	lateinit var layoutInflater: LayoutInflater

	override fun isViewFromObject(view: View, `object`: Any): Boolean {
		return (view == `object` as ConstraintLayout)
	}


	@SuppressLint("SetTextI18n")
	override fun instantiateItem(container: ViewGroup, position: Int): Any {
		layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view = layoutInflater.inflate(R.layout.item_view_pager_receive, container, false)
		views[position] = view

		val curWallet = walletList[position]
		view.apply {

			val imgQR = findViewById<ImageView>(R.id.qrImg)
			val addressWallet = curWallet.address
			try {
				val qrgEncoder =
					QRGEncoder(addressWallet, null, QRGContents.Type.TEXT, 1500)
				qrgEncoder.colorBlack = ContextCompat.getColor(context, R.color.qr_black)
				qrgEncoder.colorWhite = ContextCompat.getColor(context, R.color.qr_white)
				val bitMap = qrgEncoder.bitmap
				imgQR.setImageBitmap(bitMap)
			} catch (ex: Exception) {
				VLog.e("Exception in creating bitmap : ${ex.message}")
			}

			val coinNetwork = findViewById<TextView>(R.id.edtToken)
			coinNetwork.text = curWallet.networkType
			val coinName = findViewById<TextView>(R.id.txtCoin)
			coinName.text = curWallet.networkType

			val txtHiddenPublicKey = findViewById<TextView>(R.id.txtHiddenPublicKey)
			txtHiddenPublicKey.setText(curWallet.fingerPrint.toString())

		}
		container.addView(view)
		return view
	}

	override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
		container.removeView(`object` as ConstraintLayout)
	}


}
