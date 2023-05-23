package com.green.wallet.presentation.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class OnlyVerticalSwipeRefreshLayout(context: Context, attrs: AttributeSet?) :
	SwipeRefreshLayout(context, attrs) {
	private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
	private var prevX = 0f
	private var declined = false

	var topY = 0
	var bottomY = 0
	var isOneHomeFragment = false
	var isNFTScreen = false
	override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
		if (isOneHomeFragment)
			isEnabled = event.y.toInt() !in topY..bottomY
		if (isNFTScreen)
			isEnabled = event.y.toInt() !in topY..bottomY

		when (event.getAction()) {
			MotionEvent.ACTION_DOWN -> {
				prevX = MotionEvent.obtain(event).getX()
				declined = false // New action
			}

			MotionEvent.ACTION_MOVE -> {
				val eventX: Float = event.getX()
				val xDiff = Math.abs(eventX - prevX)
				if (declined || xDiff > touchSlop) {
					declined = true // Memorize
					return false
				}
			}
		}
		return super.onInterceptTouchEvent(event)
	}


}
