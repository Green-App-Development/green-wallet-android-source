package com.android.greenapp.presentation.custom

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Spinner
import com.android.greenapp.R
import com.android.greenapp.presentation.di.application.AppScope
import javax.inject.Inject

/**
 * Created by bekjan on 28.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@AppScope
class AnimationManager @Inject constructor(private var context: Context) {

    fun getBtnEffectAnimation() = AnimationUtils.loadAnimation(context, R.anim.btn_effect)

    fun rotateBy180Forward(v: View) {
        v.animate().rotationBy(180f).setDuration(400).start()
    }

    fun rotateBy180Backward(v: View) {
        v.animate().rotationBy(-180f).setDuration(400).start()
    }


    fun animateArrowIconCustomSpinner(spinner: CustomSpinner, icon: ImageView) {
        spinner.setSpinnerEventsListener(object : CustomSpinner.OnSpinnerEventsListener {
            override fun onSpinnerOpened(spin: Spinner?) {
                rotateBy180Forward(icon)
            }

            override fun onSpinnerClosed(spin: Spinner?) {
                rotateBy180Backward(icon)
            }

        })
    }

}