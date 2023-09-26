package com.green.wallet.presentation.custom.base

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment

fun Fragment.makeViewVisibleAndGone(view: View) {
    view.visibility = View.VISIBLE
    Handler(Looper.myLooper()!!).postDelayed({
        if (isVisible) {
            view.visibility = View.GONE
        }
    }, 2000)
}