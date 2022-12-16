package extralogic.wallet.greenapp.presentation.custom

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation


/**
 * Created by bekjan on 02.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ResizeAnimation(view: View, targetHeight: Int, startHeight: Int) :
    Animation() {
    val targetHeight: Int
    var view: View
    var startHeight: Int
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        val newHeight = (startHeight + targetHeight * interpolatedTime).toInt()
        //to support decent animation, change new heigt as Nico S. recommended in comments
        //int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
        view.layoutParams.height = newHeight
        view.requestLayout()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }

    init {
        this.view = view
        this.targetHeight = targetHeight
        this.startHeight = startHeight
    }
}