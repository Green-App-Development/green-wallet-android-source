package com.example.common.tools

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat

/**
 * Created by bekjan on 27.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */

interface OnSwipeHelper {
    fun swipedLeft()
    fun swipedRight()
    fun swipedTop()
    fun swipedBottom()
}

fun View.onSwipeListener(context: Context, swipeHelper: OnSwipeHelper) {
    this.setOnTouchListener(object : View.OnTouchListener {

        private val gestureDetector: GestureDetector

        private val SWIPE_THRESHOLD = 50
        private val SWIPE_VELOCITY_THRESHOLD = 50

        init {
            gestureDetector = GestureDetector(context, GestureListener())
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {


            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                var result = false
                try {
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                            result = true
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom()
                        } else {
                            onSwipeTop()
                        }
                        result = true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                return result
            }


        }

        fun onSwipeRight() {
            swipeHelper.swipedRight()
        }

        fun onSwipeLeft() {
            swipeHelper.swipedLeft()
        }

        fun onSwipeTop() {
            swipeHelper.swipedTop()
        }

        fun onSwipeBottom() {
            swipeHelper.swipedBottom()
        }

    })
}

fun Activity.getColorResource(resId: Int): Int = ContextCompat.getColor(this, resId)

fun Activity.getDrawableResource(resId: Int) = ContextCompat.getDrawable(this, resId)

fun Activity.getStringResource(resId: Int) = resources.getString(resId)

fun Activity.getBooleanResource(resId: Int) = resources.getBoolean(resId)


fun Activity.pxToDp(px: Int): Int {
    val displayMetrics = resources.displayMetrics
    return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Activity.getResourceAnimation(resId: Int) = AnimationUtils.loadAnimation(this, resId)


