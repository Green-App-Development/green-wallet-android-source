package com.green.wallet.presentation.main.support.faq

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.green.wallet.domain.domainmodel.FAQItem
import com.example.common.tools.VLog

/**
 * Created by bekjan on 02.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class FAQAdapter(private val activity: Activity) :
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    private val questionList = mutableListOf<FAQItem>()
    private var prevClickedTxtAnswer: TextView? = null
    private var prevRotatedImgIcon: ImageView? = null

    private var lastOneOpened = false

    fun updateQuestionList(list: List<FAQItem>) {
        questionList.clear()
        questionList.addAll(list)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.onBind(questionList[position])
    }

    override fun getItemCount() = questionList.size

    inner class FAQViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val relQuestion = v.findViewById<RelativeLayout>(R.id.rel_question)
        private val txtQuestion = v.findViewById<TextView>(R.id.txtQuestion)
        private val txtAnswer = v.findViewById<TextView>(R.id.txtAnswer)
        private val imgPlus = v.findViewById<ImageView>(R.id.img_plus)

        fun onBind(faqItem: FAQItem) {

            relQuestion.setOnClickListener { rel ->


                if (prevClickedTxtAnswer != null && txtAnswer != prevClickedTxtAnswer && lastOneOpened) {
                    prevRotatedImgIcon!!.animate().rotationBy(-45f).setDuration(500).start()
                    prevClickedTxtAnswer?.visibility = View.GONE
                }

                if (txtAnswer.visibility == View.GONE) {
                    VLog.d("Sliding down txtAnswer")
                    imgPlus.animate().rotationBy(45f).setDuration(500).start()
                    expand(txtAnswer)
                    lastOneOpened = true
                } else {
                    VLog.d("Sliding up txtAnswer")
                    collapse(txtAnswer)
                    imgPlus.animate().rotationBy(-45f).setDuration(500).start()
                    lastOneOpened = false
                }
                prevClickedTxtAnswer = txtAnswer
                prevRotatedImgIcon = imgPlus
            }

            txtAnswer.text = faqItem.answer
            txtQuestion.text = faqItem.question
        }

    }


    fun expand(view: View) {
        val animation = expandAction(view)
        view.startAnimation(animation)
    }

    private fun expandAction(view: View): Animation {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val actualheight = view.measuredHeight
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        val animation: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                view.layoutParams.height =
                    if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (actualheight * interpolatedTime).toInt()
                view.requestLayout()
            }
        }
        animation.duration = (actualheight / view.context.resources.displayMetrics.density).toLong()
        view.startAnimation(animation)
        return animation
    }

    fun collapse(view: View) {
        val actualHeight = view.measuredHeight
        val animation: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    view.visibility = View.GONE
                } else {
                    view.layoutParams.height =
                        actualHeight - (actualHeight * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }
        }
        animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong()
        view.startAnimation(animation)
    }

}
