package com.hari.gymlog.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * A horizontal stacked bar showing Protein (red), Carbs (yellow), and Fat (green).
 */
class MacroBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val proteinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF5350") // Red
    }
    private val carbsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFC107") // Amber
    }
    private val fatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66BB6A") // Green
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A000000") // Faint grey
    }

    private var protein = 0f
    private var carbs = 0f
    private var fat = 0f
    private var animFraction = 0f

    fun setMacros(proteinGrams: Int, carbsGrams: Int, fatGrams: Int) {
        protein = proteinGrams.toFloat()
        carbs = carbsGrams.toFloat()
        fat = fatGrams.toFloat()

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 800
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { anim ->
            animFraction = anim.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = protein + carbs + fat
        val h = height.toFloat()
        val w = width.toFloat()
        val radius = h / 2f

        // Background pill
        canvas.drawRoundRect(0f, 0f, w, h, radius, radius, bgPaint)

        if (total <= 0) return

        val pWidth = (protein / total) * w * animFraction
        val cWidth = (carbs / total) * w * animFraction
        val fWidth = (fat / total) * w * animFraction

        // Draw protein segment
        canvas.drawRoundRect(0f, 0f, pWidth, h, radius, radius, proteinPaint)
        // Draw carbs segment
        canvas.drawRect(pWidth, 0f, pWidth + cWidth, h, carbsPaint)
        // Draw fat segment
        canvas.drawRoundRect(pWidth + cWidth, 0f, pWidth + cWidth + fWidth, h, radius, radius, fatPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 20 // dp equivalent handled by layout
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY))
    }
}
