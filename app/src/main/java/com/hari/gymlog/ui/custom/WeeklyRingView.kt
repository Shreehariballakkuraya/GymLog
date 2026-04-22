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

class WeeklyRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#1A1976D2") // very transparent blue
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#1976D2")
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#757575")
    }

    private val rect = RectF()
    private var currentAngle = 0f
    private var targetAngle = 0f
    private var daysWorkedOut = 0
    private var totalDays = 7

    fun setProgress(daysWorked: Int, total: Int = 7) {
        daysWorkedOut = daysWorked
        totalDays = total
        targetAngle = if (total > 0) (daysWorked.toFloat() / total) * 360f else 0f

        val animator = ValueAnimator.ofFloat(0f, targetAngle)
        animator.duration = 1200
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { anim ->
            currentAngle = anim.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 30f
        rect.set(padding, padding, width - padding, height - padding)

        // Background ring
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Gradient for progress arc
        progressPaint.shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            Color.parseColor("#42A5F5"),
            Color.parseColor("#1565C0"),
            Shader.TileMode.CLAMP
        )

        // Progress ring
        canvas.drawArc(rect, -90f, currentAngle, false, progressPaint)

        // Center text (big number)
        textPaint.textSize = width * 0.22f
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawText("$daysWorkedOut", centerX, centerY + (textPaint.textSize * 0.12f), textPaint)

        // Sub text
        subTextPaint.textSize = width * 0.08f
        canvas.drawText("of $totalDays days", centerX, centerY + (textPaint.textSize * 0.6f), subTextPaint)
    }
}
