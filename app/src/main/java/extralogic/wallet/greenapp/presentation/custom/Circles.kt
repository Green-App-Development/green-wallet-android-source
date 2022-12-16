package com.example.common.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.android.greenapp.R
import com.example.common.tools.VLog

class Circles(private val con: Context, private val attr: AttributeSet) : View(con, attr) {

    private var circleEdgeColor: Int = 0
    private var circleRadius = 15f
    var usedCircleCount = 0
    private var usedCircleColor = 0
    private var wrongCirclePaintColor = 0
    var shouldBeRedAll = false

    private val circlePaint = Paint()
    private val usedCirclePaint = Paint()
    private val wrongCirclePaint = Paint()

    init {
        val typedArray = con.theme.obtainStyledAttributes(attr, R.styleable.Circles, 0, 0)
        try {

            circleEdgeColor = typedArray.getInteger(R.styleable.Circles_circleEdgeColor, 0)
            usedCircleCount = typedArray.getInteger(R.styleable.Circles_usedCircleCount, 0)
            usedCircleColor = typedArray.getInteger(R.styleable.Circles_usedCircleColor, 0)
            wrongCirclePaintColor = typedArray.getInteger(R.styleable.Circles_wrongCircleColor, 0)
            shouldBeRedAll = typedArray.getBoolean(R.styleable.Circles_shouldBeRedAll, false)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        circlePaint.color = circleEdgeColor
        circlePaint.strokeWidth = 4f
        circlePaint.style = Paint.Style.STROKE
        circlePaint.isAntiAlias = true


        usedCirclePaint.color = usedCircleColor
        usedCirclePaint.style = Paint.Style.FILL
        usedCirclePaint.isAntiAlias = true


        wrongCirclePaint.color = wrongCirclePaintColor
        wrongCirclePaint.style = Paint.Style.FILL
        wrongCirclePaint.isAntiAlias = true


        drawCircles(canvas)

    }


    private fun drawCircles(canvas: Canvas) {
        val distance = 80
        for (i in 1..6) {
            canvas.drawCircle(i.toFloat() * distance, 20f, circleRadius, circlePaint)
        }
        if (usedCircleCount > 6) {
            usedCircleCount = 6
        }
        for (i in 1..usedCircleCount) {
            canvas.drawCircle(i.toFloat() * distance, 20f, circleRadius, usedCirclePaint)
        }

        if (shouldBeRedAll) {
            for (i in 1..6) {
                canvas.drawCircle(i.toFloat() * distance, 20f, circleRadius + 2, wrongCirclePaint)
            }
        }

        invalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(550, 40)
    }


}