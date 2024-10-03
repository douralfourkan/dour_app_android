package com.douralfourkan.firstapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.douralfourkan.firstapp.R
import kotlin.math.cos
import kotlin.math.sin

class PieChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    constructor(context: Context) : this(context, null) {
        // Secondary constructor without AttributeSet
    }
    private var maleCount: Int = 0
    private var femaleCount: Int = 0
    private var totalPercentage: Float = 0f

    private val paintMale: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintFemale: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)
        maleCount = typedArray.getInt(R.styleable.PieChartView_maleCount, 0)
        femaleCount = typedArray.getInt(R.styleable.PieChartView_femaleCount, 0)
        typedArray.recycle()

// Custom color for male (e.g., dark blue)
        paintMale.color = Color.parseColor("#00bcd4")

// Custom color for female (e.g., coral)
        paintFemale.color = Color.parseColor("#FF69B4")



        textPaint.color = Color.BLACK
        textPaint.textSize = 20f
        textPaint.textAlign = Paint.Align.CENTER

        // Apply default counts
        updatePercentages()
    }

    private fun updatePercentages() {
        val total = maleCount + femaleCount
        totalPercentage = if (total > 0) (maleCount.toFloat() / total + femaleCount.toFloat() / total) * 100 else 0f
        invalidate()
    }

    fun setCounts(maleCount: Int, femaleCount: Int) {
        this.maleCount = maleCount
        this.femaleCount = femaleCount
        updatePercentages()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = 220f

        // Calculate total percentage
        val total = maleCount + femaleCount
        val totalPercentage = if (total > 0) (maleCount.toFloat() / total + femaleCount.toFloat() / total) * 100 else 0f

        // Calculate individual percentages
        val malePercentage = if (totalPercentage > 0) (maleCount.toFloat() / total) * 100 else 0f
        val femalePercentage = if (totalPercentage > 0) (femaleCount.toFloat() / total) * 100 else 0f

        // Draw the arc for males
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f,
            360f * malePercentage / 100f,
            true,
            paintMale
        )

        // Draw the arc for females
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f + 360f * malePercentage / 100f,
            360f * femalePercentage / 100f,
            true,
            paintFemale
        )

        // Draw the percentage text in each region
        val maleText = "ذكور\n$maleCount (${malePercentage.toInt()}%)"
        val femaleText = "إناث\n$femaleCount (${femalePercentage.toInt()}%)"


        drawTextInRegion(
            canvas,
            centerX,
            centerY,
            radius,
            -90f,
            360f * malePercentage / 100f,
            maleText
        )
        drawTextInRegion(
            canvas,
            centerX,
            centerY,
            radius,
            -90f + 360f * malePercentage / 100f,
            360f * femalePercentage / 100f,
            femaleText
        )
    }

    private fun drawTextInRegion(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float,
        text: String
    ) {
        val angle = startAngle + sweepAngle / 2f
        val textX = centerX + radius * 0.5f * cos(Math.toRadians(angle.toDouble())).toFloat()
        val textY = centerY + radius * 0.5f * sin(Math.toRadians(angle.toDouble())).toFloat()

        textPaint.textSize = 30f // Adjust the text size as needed
        textPaint.isFakeBoldText = true
        canvas.drawText(text, textX, textY, textPaint)
    }
}
