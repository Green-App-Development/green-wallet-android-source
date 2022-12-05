package com.android.greenapp.presentation.custom

import android.app.Activity
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

	fun rotateBy180Forward(v: View,activity:Activity) {
		val animation = AnimationUtils.loadAnimation(activity, R.anim.rotate_forward_180)
		animation.fillAfter = true
		animation.isFillEnabled = true
		v.startAnimation(animation)
	}

	fun rotateBy180Backward(v: View,activity:Activity) {
		val animation = AnimationUtils.loadAnimation(activity, R.anim.rotate_backward_180)
		animation.fillAfter = true
		animation.isFillEnabled = true
		v.startAnimation(animation)
	}


	fun animateArrowIconCustomSpinner(spinner: CustomSpinner, icon: ImageView,activity: Activity) {
		spinner.setSpinnerEventsListener(object : CustomSpinner.OnSpinnerEventsListener {
			override fun onSpinnerOpened(spin: Spinner?) {
				rotateBy180Forward(icon,activity)
			}

			override fun onSpinnerClosed(spin: Spinner?) {
				rotateBy180Backward(icon,activity)
			}

		})
	}

}
