package com.green.wallet.presentation.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Spinner
import com.green.wallet.R
import com.green.wallet.presentation.di.application.AppScope
import javax.inject.Inject


@AppScope
class AnimationManager @Inject constructor(private var context: Context) {

	fun getBtnEffectAnimation() = AnimationUtils.loadAnimation(context, R.anim.btn_effect)

	fun rotateBy180Forward(v: View, activity: Activity) {
		val animation = AnimationUtils.loadAnimation(activity, R.anim.rotate_forward_180)
		animation.fillAfter = true
		animation.isFillEnabled = true
		v.startAnimation(animation)
	}

	fun rotateBy180Backward(v: View, activity: Activity) {
		val animation = AnimationUtils.loadAnimation(activity, R.anim.rotate_backward_180)
		animation.fillAfter = true
		animation.isFillEnabled = true
		v.startAnimation(animation)
	}


	fun rotateBy180ForwardNoAnimation(v: View, activity: Activity) {
		val animation = AnimationUtils.loadAnimation(activity, R.anim.rotate_forward_180_no_anim)
		animation.fillAfter = true
		animation.isFillEnabled = true
		v.startAnimation(animation)
	}

	fun animateArrowIconCustomSpinner(spinner: CustomSpinner, icon: ImageView, activity: Activity) {
		spinner.setSpinnerEventsListener(object : CustomSpinner.OnSpinnerEventsListener {
			override fun onSpinnerOpened(spin: Spinner?) {
				rotateBy180Forward(icon, activity)
			}

			override fun onSpinnerClosed(spin: Spinner?) {
				rotateBy180Backward(icon, activity)
			}

		})
	}

	var prevAnimSet: AnimatorSet? = null
		private set

	fun moveViewToRightByWidth(view: View, width: Int): Boolean {
		if (prevAnimSet?.isRunning == true)
			return true
		val centerX = view.x
		val desiredX = centerX + width
		val translateX = ObjectAnimator.ofFloat(view, "translationX", view.x, desiredX)
		translateX.duration = 300
		val animSet = AnimatorSet()
		animSet.playTogether(translateX)
		prevAnimSet = animSet
		animSet.start()
		return false
	}

	fun moveViewToRightByWidthNoAnim(view: View, width: Int): Boolean {
		if (prevAnimSet?.isRunning == true)
			return true
		val centerX = view.x
		val desiredX = centerX + width
		val translateX = ObjectAnimator.ofFloat(view, "translationX", view.x, desiredX)
		translateX.duration = 1
		val animSet = AnimatorSet()
		animSet.playTogether(translateX)
		prevAnimSet = animSet
		animSet.start()
		return false
	}

	fun moveViewToLeftByWidth(view: View, width: Int): Boolean {
		if (prevAnimSet?.isRunning == true)
			return true
		val centerX = view.x
		val desiredX = centerX - width
		val translateX = ObjectAnimator.ofFloat(view, "translationX", view.x, desiredX)
		translateX.duration = 300
		val animSet = AnimatorSet()
		animSet.playTogether(translateX)
		prevAnimSet = animSet
		animSet.start()
		return false
	}

}
