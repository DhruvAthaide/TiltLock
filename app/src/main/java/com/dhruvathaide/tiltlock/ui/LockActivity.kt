package com.dhruvathaide.tiltlock.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.GestureRepository
import com.dhruvathaide.tiltlock.data.TiltSensorManager

class LockActivity : AppCompatActivity() {

    private lateinit var viewModel: LockViewModel
    private lateinit var sensorManager: TiltSensorManager
    private lateinit var vaultCard: View
    private lateinit var iris: ImageView
    private lateinit var debugText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        vaultCard = findViewById(R.id.vaultCard)
        iris = findViewById(R.id.iris)
        debugText = findViewById(R.id.debugText)

        // Setup Dependencies
        sensorManager = TiltSensorManager(this)
        val repository = GestureRepository(this)
        val factory = LockViewModelFactory(repository, sensorManager)
        viewModel = ViewModelProvider(this, factory)[LockViewModel::class.java]

        if (!repository.hasGesture()) {
            // First Launch / No Gesture -> Force Setup
            startActivity(Intent(this, GestureSetupActivity::class.java))
            finish()
            return
        }

        setupObservers()
        // Removed setupListeners (Setup button is gone from Lock Screen)
    }



    private fun setupObservers() {
        // Observe Raw Gyro for Parallax
        sensorManager.rawGyro.observe(this) { (x, y) ->
            applyParallax(x, y)
        }

        // Observe Lock State
        viewModel.lockState.observe(this) { state ->
            when (state) {
                LockState.LOCKED -> resetIris()
                LockState.SUCCESS -> unlockSuccess()
                LockState.ERROR -> unlockError()
                else -> {}
            }
        }

        // Observe Input for visual feedback (Iris Glow)
        viewModel.currentInput.observe(this) { sequence ->
            if (sequence.isNotEmpty()) {
                pulseIris()
                performHapticFeedback()
            }
        }
    }

    private fun applyParallax(x: Float, y: Float) {
        // x and y are rotation speeds/directions. 
        // Ideally parallax uses orientation (Rotation Vector), but Gyro rate can simulate dynamic movement.
        // Let's smooth it slightly or just apply raw for "live" feel.
        // Inverting axes for natural "looking window" feel.
        vaultCard.rotationX = x * 2f
        vaultCard.rotationY = y * 2f
    }

    private fun pulseIris() {
        iris.animate()
            .scaleX(1.2f).scaleY(1.2f)
            .setDuration(100)
            .withEndAction {
                iris.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }
            .start()
        
        val neonCyan = ContextCompat.getColor(this, R.color.neon_cyan)
        iris.backgroundTintList = ColorStateList.valueOf(neonCyan)
    }

    private fun resetIris() {
        iris.backgroundTintList = ColorStateList.valueOf(Color.GRAY) // Dim state
        iris.clearAnimation()
    }

    private fun unlockSuccess() {
        val neonCyan = ContextCompat.getColor(this, R.color.neon_cyan)
        iris.setColorFilter(neonCyan) // Fill properly
        iris.backgroundTintList = ColorStateList.valueOf(neonCyan)
        
        iris.animate().alpha(0f).scaleX(3f).scaleY(3f).setDuration(500).withEndAction {
            startActivity(Intent(this, SecureFolderActivity::class.java))
            finish()
        }.start()
    }

    private fun unlockError() {
        // Red Pulse
        val errorRed = ContextCompat.getColor(this, R.color.error_red)
        iris.backgroundTintList = ColorStateList.valueOf(errorRed)

        // Shake
        val shake = ObjectAnimator.ofFloat(vaultCard, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = 500
        shake.start()
        
        // Haptic Error
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))

        // Reset after animation
        vaultCard.postDelayed({
            viewModel.reset()
        }, 600)
    }

    private fun performHapticFeedback() {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(50, 150)) // Quick tick
    }

    override fun onResume() {
        super.onResume()
        sensorManager.start()
        viewModel.reset() // Always relock on resume
        // Reload gesture in case it changed
        viewModel.reloadGesture()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
    }
}
