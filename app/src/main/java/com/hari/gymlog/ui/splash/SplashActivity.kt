package com.hari.gymlog.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hari.gymlog.R
import com.hari.gymlog.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val tvAppName = findViewById<TextView>(R.id.tvAppName)

        // Start invisible
        tvAppName.alpha = 0f
        tvAppName.scaleX = 0.5f
        tvAppName.scaleY = 0.5f

        // Animate: fade in + scale up with overshoot
        val fadeIn = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f).apply {
            duration = 800
        }
        val scaleX = ObjectAnimator.ofFloat(tvAppName, "scaleX", 0.5f, 1f).apply {
            duration = 1000
            interpolator = OvershootInterpolator(1.5f)
        }
        val scaleY = ObjectAnimator.ofFloat(tvAppName, "scaleY", 0.5f, 1f).apply {
            duration = 1000
            interpolator = OvershootInterpolator(1.5f)
        }

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2000)
    }
}
