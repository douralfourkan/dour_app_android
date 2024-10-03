package com.douralfourkan.firstapp
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.douralfourkan.firstapp.R

class LoadingCircleOnImage(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var percentage: Int
    private var paint: Paint = Paint()
    private var paintBackground: Paint = Paint()
    private var textPaint: Paint = Paint()

    init {
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 30f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.setShadowLayer(10f, 5f, 5f, Color.GRAY)

        paintBackground.color = Color.GRAY
        paintBackground.style = Paint.Style.STROKE
        paintBackground.strokeWidth = 25f
        paintBackground.strokeCap = Paint.Cap.ROUND
        paintBackground.strokeJoin = Paint.Join.ROUND
        paintBackground.setShadowLayer(10f, 5f, 5f, Color.GRAY)

        textPaint.color = Color.BLACK
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingCircleOnImage)
        percentage = typedArray.getInt(R.styleable.LoadingCircleOnImage_percentage, 0)
        typedArray.recycle()
    }

    fun setPercentage(newPercentage: Int) {
        percentage = newPercentage
        // Trigger a redraw of the view when the percentage changes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = 100f


        // Draw background arc
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f,
            360f * 100 / 100f,
            false,
            paintBackground
        )

        // Draw the arc
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f,
            360f * percentage / 100f,
            false,
            paint
        )
        // Draw the percentage text in the middle
        var numberofPages = percentage/5
        val percentageText = "$numberofPages"
        canvas.drawText(percentageText, centerX+5f, centerY+10f, textPaint)

    }



}
