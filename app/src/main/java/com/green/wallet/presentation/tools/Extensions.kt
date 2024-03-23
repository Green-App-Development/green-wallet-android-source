package com.green.wallet.presentation.tools

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.common.tools.formattedTime
import com.green.wallet.R
import com.green.wallet.databinding.NftImgPlaceholderBinding
import com.green.wallet.presentation.custom.getTranslatedMonth
import com.green.wallet.presentation.main.MainActivity


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
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                var result = false
                try {
                    val diffY = e2.y - (e1?.y ?: 0f)
                    val diffX = e2.x - (e1?.x ?: 0f)
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

fun Context.dpToPx(dp: Int): Int {
    val density = resources.displayMetrics.density
    val pxValue = (dp * density).toInt()
    return pxValue
}

fun Activity.getResourceAnimation(resId: Int) = AnimationUtils.loadAnimation(this, resId)


fun ImageView.loadSvg(url: String) {
    val imageLoader = ImageLoader.Builder(this.context)
        .componentRegistry { add(SvgDecoder(this@loadSvg.context)) }
        .build()

    val request = ImageRequest.Builder(this.context)
        .crossfade(true)
        .crossfade(500)
        .data(url)
        .target(this)
        .build()

    imageLoader.enqueue(request)
}

fun View.preventDoubleClick(interval: Long = 1000) {
    isEnabled = false
    postDelayed({
        isEnabled = true
    }, interval)
}

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

fun Fragment.getMainActivity(): MainActivity {
    return requireActivity() as MainActivity
}


fun Activity.getCustomProgressLayoutWithParams(size: Int, thickness: Int): View {
    val binding = NftImgPlaceholderBinding.inflate(layoutInflater)
    binding.progressBar.apply {
        indicatorSize = size
        trackThickness = thickness
    }
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
    binding.root.layoutParams = layoutParams
    return binding.root
}

fun Activity.makeGreenDuringFocus(txt: TextView) {
    txt.setTextColor(getColorResource(R.color.green))
}

fun Activity.makeGreyDuringNonFocus(txt: TextView) {
    txt.setTextColor(getColorResource(R.color.grey_txt_color))
}


fun Activity.copyToClipBoard(data: String) {
    val clipBoard =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(
        "label",
        data
    )
    clipBoard.setPrimaryClip(clip)
}


fun Context.copyToClipBoard(data: String) {
    val clipBoard =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(
        "label",
        data
    )
    clipBoard.setPrimaryClip(clip)
}

fun Activity.formatDateWithMonthInWord(timeCreated: Long): String {
    val format = formattedTime(timeCreated)
    val split = format.split(" ")
    val dateMonthYear = split[0].split('-')
    val getTranslatedMonth = getTranslatedMonth(this, dateMonthYear[1].toInt())
    var day = dateMonthYear[0]
    if (day.length == 1)
        day = "0$day"
    val hourTime = split[1].split(":").subList(0, 2)
    return "$day $getTranslatedMonth, $hourTime"
}












