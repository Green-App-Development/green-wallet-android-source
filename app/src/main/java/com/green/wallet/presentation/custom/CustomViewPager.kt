package com.green.wallet.presentation.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class CustomViewPager(context: Context, attr: AttributeSet) : ViewPager(context, attr) {

    var lastPageEnabled = true
    var lastPageIndex = 10


    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return !(!lastPageEnabled && currentItem >= lastPageIndex - 1)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return !(!lastPageEnabled && currentItem >= lastPageIndex - 1)
    }

}
