package com.dhruvathaide.tiltlock.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.AccessLogRepository
import com.dhruvathaide.tiltlock.data.GestureRepository
import com.dhruvathaide.tiltlock.data.LogType
import com.dhruvathaide.tiltlock.data.SettingsRepository
import com.dhruvathaide.tiltlock.data.TiltSensorManager
import com.dhruvathaide.tiltlock.utils.HapticManager
import com.dhruvathaide.tiltlock.utils.SoundManager

class LockActivity : AppCompatActivity() {

    private lateinit var viewModel: LockViewModel
    private lateinit var sensorManager: TiltSensorManager
    private lateinit var vaultCard: View
    private lateinit var iris: ImageView
    private lateinit var debugText: TextView
    
    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    private lateinit var settings: SettingsRepository

    private lateinit var accessLogRepository: AccessLogRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)
        
        // ... (views)
        vaultCard = findViewById(R.id.vaultCard)
        iris = findViewById(R.id.iris)
        debugText = findViewById(R.id.debugText)

        // Setup Dependencies
        sensorManager = TiltSensorManager(this)
        val repository = GestureRepository(this)
        val factory = LockViewModelFactory(repository, sensorManager)
        viewModel = ViewModelProvider(this, factory)[LockViewModel::class.java]
        
        soundManager = SoundManager(this)
        hapticManager = HapticManager(this)
        settings = SettingsRepository(this)
        accessLogRepository = AccessLogRepository(this)

        if (!repository.hasGesture()) {
            startActivity(Intent(this, GestureSetupActivity::class.java))
            finish()
            return
        }

        setupObservers()
    }

    // ... (observers and parallax)

    private fun unlockSuccess() {
        val neonCyan = ContextCompat.getColor(this, R.color.neon_cyan)
        iris.setColorFilter(neonCyan) 
        iris.backgroundTintList = ColorStateList.valueOf(neonCyan)
        
        if (settings.soundEnabled) soundManager.play(SoundManager.SOUND_ACCESS_GRANTED)
        if (settings.hapticsEnabled) hapticManager.playSuccess()
        accessLogRepository.logEvent(LogType.SUCCESS)
        
        iris.animate().alpha(0f).scaleX(3f).scaleY(3f).setDuration(500).withEndAction {
            startActivity(Intent(this, SecureFolderActivity::class.java))
            finish()
        }.start()
    }

    private fun unlockError() {
        // Red Pulse
        val errorRed = ContextCompat.getColor(this, R.color.error_red)
        iris.backgroundTintList = ColorStateList.valueOf(errorRed)
        iris.setColorFilter(errorRed)

        // Sound/Haptic
        if (settings.soundEnabled) soundManager.play(SoundManager.SOUND_ACCESS_DENIED)
        if (settings.glitchEnabled) soundManager.play(SoundManager.SOUND_GLITCH)
        if (settings.hapticsEnabled) hapticManager.playError()
        accessLogRepository.logEvent(LogType.FAILURE)

        // Glitch Shake
        if (settings.glitchEnabled) {
            val shake = ObjectAnimator.ofFloat(vaultCard, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            shake.duration = 500
            shake.start()
            
            // Glitch RGB Split Simulation
            iris.postDelayed({ iris.setColorFilter(Color.CYAN) }, 100)
            iris.postDelayed({ iris.setColorFilter(Color.MAGENTA) }, 200)
            iris.postDelayed({ iris.setColorFilter(errorRed) }, 300)
        }

        // Reset after animation
        vaultCard.postDelayed({
            viewModel.reset()
        }, 600)
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
            }
            
            // Pattern Visibility logic
            if (settings.showPattern) {
               debugText.text = sequence.joinToString(" >> ") { it.name }
               debugText.alpha = 1.0f
            } else {
               debugText.text = getString(R.string.msg_authenticate)
               debugText.alpha = 0.7f
            }
        }
    }

    private fun applyParallax(x: Float, y: Float) {
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
        
        // Feedback
        if (settings.soundEnabled) soundManager.play(SoundManager.SOUND_SCAN)
        if (settings.hapticsEnabled) hapticManager.playTick()
    }

    private fun resetIris() {
        iris.backgroundTintList = ColorStateList.valueOf(Color.GRAY) // Dim state
        iris.clearAnimation()
        iris.setColorFilter(Color.parseColor("#40FFFFFF")) // Reset tint
    }

    override fun onResume() {
        super.onResume()
        sensorManager.start()
        viewModel.reset() // Always relock on resume
        viewModel.reloadGesture()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
