package com.hari.gymlog.reminder

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hari.gymlog.data.db.GymLogDatabase
import com.hari.gymlog.data.db.entity.WorkoutEntity
import com.hari.gymlog.databinding.ActivityReminderSessionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderSessionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXERCISE     = "extra_exercise"
        const val EXTRA_MODE         = "extra_mode"
        const val EXTRA_TARGET_REPS  = "extra_target_reps"
        const val EXTRA_COUNTDOWN_SECS = "extra_countdown_secs"
        const val EXTRA_PACE_SECS    = "extra_pace_secs"
        const val EXTRA_HOLD_SECS    = "extra_hold_secs"
    }

    private lateinit var binding: ActivityReminderSessionBinding

    private var exerciseName  = "Push-ups"
    private var mode          = ReminderSettingsFragment.MODE_REPS
    private var targetReps    = 10
    private var countdownSecs = 10
    private var paceSecs      = 3
    private var holdSecs      = 60

    private var repsRemaining = 0
    private var isPaused      = false

    // Two timers: one for the get-ready phase, one for the rep guide
    private var getReadyTimer: CountDownTimer? = null
    private var repGuideTimer: CountDownTimer? = null

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exerciseName  = intent.getStringExtra(EXTRA_EXERCISE) ?: "Push-ups"
        mode          = intent.getStringExtra(EXTRA_MODE) ?: ReminderSettingsFragment.MODE_REPS
        targetReps    = intent.getIntExtra(EXTRA_TARGET_REPS, 10)
        countdownSecs = intent.getIntExtra(EXTRA_COUNTDOWN_SECS, 10)
        paceSecs      = intent.getIntExtra(EXTRA_PACE_SECS, 3)
        holdSecs      = intent.getIntExtra(EXTRA_HOLD_SECS, 60)

        toneGenerator = try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        } catch (_: Exception) { null }

        showConfirmation()
    }

    // ─── SCREEN 1: Confirmation + adjustable reps ─────────────────────────────

    private fun showConfirmation() {
        binding.layoutConfirmation.visibility = View.VISIBLE
        binding.layoutCountdown.visibility    = View.GONE
        binding.layoutRepCounter.visibility   = View.GONE

        binding.tvReadyTitle.text = "Time for $exerciseName!"
        binding.tvPaceInfo.text   = "Pace: 1 rep every $paceSecs sec  ·  ~${targetReps * paceSecs}s total"
        updateRepsConfirmDisplay()

        binding.btnIncRepsConfirm.setOnClickListener {
            targetReps++
            updateRepsConfirmDisplay()
        }
        binding.btnDecRepsConfirm.setOnClickListener {
            if (targetReps > 1) { targetReps--; updateRepsConfirmDisplay() }
        }
        binding.btnYes.setOnClickListener { startGetReady() }
        binding.btnNo.setOnClickListener  { finish() }
    }

    private fun updateRepsConfirmDisplay() {
        binding.tvRepsAdjust.text = targetReps.toString()
        // Show hold info in subtitle when time mode
        if (mode == ReminderSettingsFragment.MODE_TIME) {
            binding.tvPaceInfo.text = "Hold for ${holdSecs}s  ·  ${exerciseName}"
            binding.btnIncRepsConfirm.visibility = View.GONE
            binding.btnDecRepsConfirm.visibility = View.GONE
            binding.tvRepsAdjust.text            = "${holdSecs}s"
        } else {
            binding.btnIncRepsConfirm.visibility = View.VISIBLE
            binding.btnDecRepsConfirm.visibility = View.VISIBLE
            binding.tvPaceInfo.text = "Pace: 1 rep every $paceSecs sec  ·  ~${targetReps * paceSecs}s total"
        }
    }

    // ─── SCREEN 2: Get-Ready countdown (with beeps) ──────────────────────────

    private fun startGetReady() {
        binding.layoutConfirmation.visibility = View.GONE
        binding.layoutCountdown.visibility    = View.VISIBLE
        binding.layoutRepCounter.visibility   = View.GONE

        val totalMs = countdownSecs * 1000L

        getReadyTimer = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000).toInt() + 1
                binding.tvCountdownNumber.text = remaining.toString()
                pulseView(binding.tvCountdownNumber, 1.4f)

                // Last 3s → sharp urgent beep, otherwise soft
                if (remaining <= 3) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                } else {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                }
            }

            override fun onFinish() {
                binding.tvCountdownNumber.text = "GO!"
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 500)
                pulseView(binding.tvCountdownNumber, 1.8f)
                // Route to correct screen based on mode
                if (mode == ReminderSettingsFragment.MODE_TIME) {
                    binding.root.postDelayed({ startHoldTimer() }, 600)
                } else {
                    binding.root.postDelayed({ startRepGuide() }, 600)
                }
            }
        }.start()
    }

    // ─── SCREEN 3A: Hold Timer (Planks / Wall Sits / etc.) ───────────────────

    private fun startHoldTimer() {
        binding.layoutConfirmation.visibility = View.GONE
        binding.layoutCountdown.visibility    = View.GONE
        binding.layoutRepCounter.visibility   = View.VISIBLE

        // Re-use the repCounter layout: exercise name, label, big number, progress, pause/stop
        binding.tvRepExerciseName.text = exerciseName
        binding.tvRepLabel.text        = "Hold for ${holdSecs}s"
        binding.tvRepCount.text        = holdSecs.toString()
        binding.progressReps.max       = holdSecs
        binding.progressReps.progress  = 0
        binding.progressPace.visibility = View.GONE   // no per-rep bar needed
        isPaused = false

        var secsLeft = holdSecs

        repGuideTimer = object : CountDownTimer(holdSecs * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isPaused) return
                secsLeft = (millisUntilFinished / 1000).toInt() + 1

                binding.tvRepCount.text        = secsLeft.toString()
                binding.tvRepLabel.text        = "$secsLeft seconds left"
                binding.progressReps.progress  = holdSecs - secsLeft

                // Urgent beep last 5 seconds
                if (secsLeft <= 5) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
                    pulseView(binding.tvRepCount, 1.3f)
                }
            }

            override fun onFinish() {
                binding.tvRepCount.text       = "✓"
                binding.tvRepLabel.text       = "Hold complete! 🎉"
                binding.progressReps.progress = holdSecs
                binding.tvRepCount.setTextColor(getColor(android.R.color.holo_green_dark))

                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 800)
                pulseView(binding.tvRepCount, 2.0f)
                binding.btnPauseResume.text      = "✅ Done!"
                binding.btnPauseResume.isEnabled = false

                // Log as duration-based workout
                saveWorkoutToHistory(holdSecs)
            }
        }.start()

        binding.btnPauseResume.setOnClickListener {
            if (isPaused) {
                isPaused = false
                binding.btnPauseResume.text = "⏸ Pause"
                // Restart with remaining time
                repGuideTimer?.cancel()
                repGuideTimer = object : CountDownTimer(secsLeft * 1000L, 1000) {
                    override fun onTick(ms: Long) {
                        if (isPaused) return
                        secsLeft = (ms / 1000).toInt() + 1
                        binding.tvRepCount.text       = secsLeft.toString()
                        binding.tvRepLabel.text       = "$secsLeft seconds left"
                        binding.progressReps.progress = holdSecs - secsLeft
                        if (secsLeft <= 5) {
                            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 120)
                            pulseView(binding.tvRepCount, 1.3f)
                        }
                    }
                    override fun onFinish() {
                        binding.tvRepCount.text       = "✓"
                        binding.tvRepLabel.text       = "Hold complete! 🎉"
                        binding.progressReps.progress = holdSecs
                        binding.tvRepCount.setTextColor(getColor(android.R.color.holo_green_dark))
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 800)
                        pulseView(binding.tvRepCount, 2.0f)
                        binding.btnPauseResume.text      = "✅ Done!"
                        binding.btnPauseResume.isEnabled = false
                        saveWorkoutToHistory(holdSecs)
                    }
                }.start()
            } else {
                isPaused = true
                binding.btnPauseResume.text = "▶ Resume"
                repGuideTimer?.cancel()
            }
        }
        binding.btnStop.setOnClickListener {
            val held = holdSecs - secsLeft
            if (held > 0) saveWorkoutToHistory(held)
            finish()
        }
    }

    // ─── SCREEN 3B: Auto Rep Guide ────────────────────────────────────────────

    private fun startRepGuide() {
        binding.layoutConfirmation.visibility = View.GONE
        binding.layoutCountdown.visibility    = View.GONE
        binding.layoutRepCounter.visibility   = View.VISIBLE

        repsRemaining = targetReps
        binding.tvRepExerciseName.text = exerciseName
        binding.progressReps.max       = targetReps
        binding.progressReps.progress  = 0
        binding.progressPace.max       = 100
        binding.progressPace.progress  = 0
        isPaused = false

        updateRepDisplay()
        scheduleRepTimer()

        binding.btnPauseResume.setOnClickListener {
            if (isPaused) resume() else pause()
        }
        // "Finish Set" — save however many reps are done so far
        binding.btnStop.setOnClickListener {
            val done = targetReps - repsRemaining
            if (done > 0) saveWorkoutToHistory(done)
            finish()
        }
    }

    /**
     * Schedules a CountDownTimer for ALL remaining reps at the current pace.
     * Each tick = 50ms for smooth pace bar animation.
     * Every [paceSecs * 1000ms] we trigger a rep.
     */
    private fun scheduleRepTimer() {
        repGuideTimer?.cancel()

        val totalMs    = repsRemaining * paceSecs * 1000L
        val paceMs     = paceSecs * 1000L
        var paceElapsed = 0L

        repGuideTimer = object : CountDownTimer(totalMs, 50) {
            override fun onTick(millisUntilFinished: Long) {
                if (isPaused) return

                val elapsed = totalMs - millisUntilFinished
                paceElapsed = elapsed % paceMs

                // Smooth pace bar (fills 0→100 within each rep window)
                binding.progressPace.progress = ((paceElapsed.toFloat() / paceMs) * 100).toInt()

                // Trigger rep cue at each pace boundary
                val repsDone = (elapsed / paceMs).toInt()
                val newRemaining = (targetReps - repsDone).coerceAtLeast(0)

                if (newRemaining != repsRemaining) {
                    repsRemaining = newRemaining
                    updateRepDisplay()

                    // Beep cue
                    if (repsRemaining > 0) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                        pulseView(binding.tvRepCount, 1.35f)
                    }
                }
            }

            override fun onFinish() {
                repsRemaining = 0
                binding.progressPace.progress = 100
                updateRepDisplay()

                // Celebration beep + animation
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 700)
                pulseView(binding.tvRepCount, 2.0f)

                binding.btnPauseResume.text = "✅ Done!"
                binding.btnPauseResume.isEnabled = false

                // Auto-save to workout history
                saveWorkoutToHistory(targetReps)
            }
        }.start()
    }

    private fun pause() {
        isPaused = true
        binding.btnPauseResume.text = "▶ Resume"
        repGuideTimer?.cancel()
    }

    private fun resume() {
        isPaused = false
        binding.btnPauseResume.text = "⏸ Pause"
        scheduleRepTimer()   // restart from current repsRemaining
    }

    private fun updateRepDisplay() {
        val isDone = repsRemaining == 0

        binding.tvRepCount.text = if (isDone) "✓" else repsRemaining.toString()

        binding.tvRepLabel.text = when {
            isDone             -> "All $targetReps reps done! 🎉"
            repsRemaining == 1 -> "Last rep!"
            else               -> "$repsRemaining reps left"
        }

        binding.progressReps.progress = targetReps - repsRemaining

        val color = if (isDone) {
            getColor(android.R.color.holo_green_dark)
        } else {
            getColor(com.google.android.material.R.color.design_default_color_primary)
        }
        binding.tvRepCount.setTextColor(color)
    }

    // ─── Save to workout history ──────────────────────────────────────────────

    private fun saveWorkoutToHistory(completedReps: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val durationMinutes = (completedReps * paceSecs) / 60  // approx

        val workout = WorkoutEntity(
            date         = today,
            exerciseName = exerciseName,
            muscleGroup  = inferMuscleGroup(exerciseName),
            sets         = 1,
            reps         = completedReps,
            weight       = 0.0,   // bodyweight exercise
            duration     = durationMinutes.coerceAtLeast(1)
        )

        lifecycleScope.launch(Dispatchers.IO) {
            GymLogDatabase.getDatabase(applicationContext)
                .workoutDao()
                .insert(workout)
        }
    }

    /** Best-guess muscle group from exercise name */
    private fun inferMuscleGroup(name: String): String = when {
        name.contains("push", ignoreCase = true)  -> "Chest"
        name.contains("pull", ignoreCase = true)  -> "Back"
        name.contains("squat", ignoreCase = true) -> "Legs"
        name.contains("lunge", ignoreCase = true) -> "Legs"
        name.contains("sit", ignoreCase = true)   -> "Core"
        name.contains("burp", ignoreCase = true)  -> "Full Body"
        name.contains("dip", ignoreCase = true)   -> "Triceps"
        name.contains("jump", ignoreCase = true)  -> "Cardio"
        else                                       -> "General"
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun pulseView(view: View, scale: Float) {
        val sx = ObjectAnimator.ofFloat(view, "scaleX", scale, 1f)
        val sy = ObjectAnimator.ofFloat(view, "scaleY", scale, 1f)
        AnimatorSet().apply {
            playTogether(sx, sy)
            duration = 350
            interpolator = OvershootInterpolator(2f)
            start()
        }
    }

    override fun onDestroy() {
        getReadyTimer?.cancel()
        repGuideTimer?.cancel()
        toneGenerator?.release()
        super.onDestroy()
    }
}
