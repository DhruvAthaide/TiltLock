package com.dhruvathaide.tiltlock.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.GestureRepository
import com.dhruvathaide.tiltlock.data.TiltDirection
import com.dhruvathaide.tiltlock.data.TiltSensorManager

class GestureSetupActivity : AppCompatActivity() {

    private lateinit var sensorManager: TiltSensorManager
    private lateinit var repository: GestureRepository
    
    private lateinit var tvSequence: TextView
    private lateinit var btnRecord: Button
    private lateinit var btnSave: Button
    private lateinit var btnClear: Button
    private lateinit var visualizerCard: CardView
    
    private var isRecording = false
    private val recordedSequence = mutableListOf<TiltDirection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_setup)

        tvSequence = findViewById(R.id.gestureSequence)
        btnRecord = findViewById(R.id.btnRecord)
        btnSave = findViewById(R.id.btnSave)
        btnClear = findViewById(R.id.btnClear)
        visualizerCard = findViewById(R.id.visualizerCard)

        sensorManager = TiltSensorManager(this)
        repository = GestureRepository(this)

        setupListeners()
        setupSensor()
        updateUIState()
    }

    private fun setupListeners() {
        btnRecord.setOnClickListener {
            toggleRecording()
        }

        btnSave.setOnClickListener {
            if (recordedSequence.isNotEmpty()) {
                repository.saveGesture(recordedSequence)
                // Haptic Success
                val v = getSystemService(Vibrator::class.java)
                v?.vibrate(VibrationEffect.createOneShot(200, 255))
                finish() 
            }
        }

        btnClear.setOnClickListener {
            recordedSequence.clear()
            // Shake visualizer
            val shake = ObjectAnimator.ofFloat(visualizerCard, "translationX", 0f, 20f, -20f, 0f)
            shake.duration = 300
            shake.start()
            
            updateSequenceText()
        }
    }

    private fun toggleRecording() {
        isRecording = !isRecording
        if (isRecording) {
            btnRecord.text = "STOP_RECORDING"
            btnRecord.setTextColor(ContextCompat.getColor(this, R.color.error_red))
            recordedSequence.clear()
            updateSequenceText()
            sensorManager.start()
            
            // Pulse visualizer
            visualizerCard.animate().alpha(1f).scaleX(1.05f).scaleY(1.05f).setDuration(200).start()
        } else {
            btnRecord.text = "INIT_RECORD"
            btnRecord.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
            sensorManager.stop()
            visualizerCard.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }
    }

    private fun setupSensor() {
        sensorManager.tiltEvent.observe(this) { direction ->
            if (isRecording) {
                recordedSequence.add(direction)
                updateSequenceText()
                performHaptic()
                animateTilt(direction)
            }
        }
    }

    private fun animateTilt(direction: TiltDirection) {
        // Tilt the card visually to match input
        val rotX = when(direction) {
            TiltDirection.UP -> -10f
            TiltDirection.DOWN -> 10f
            else -> 0f
        }
        val rotY = when(direction) {
            TiltDirection.LEFT -> -10f
            TiltDirection.RIGHT -> 10f
            else -> 0f
        }
        
        visualizerCard.animate()
            .rotationX(rotX)
            .rotationY(rotY)
            .setDuration(150)
            .withEndAction {
                visualizerCard.animate().rotationX(0f).rotationY(0f).setDuration(150).start()
            }
            .start()
    }

    private fun updateSequenceText() {
        if (recordedSequence.isEmpty()) {
            tvSequence.text = "[ WAITING FOR INPUT ]"
            tvSequence.alpha = 0.5f
        } else {
            tvSequence.text = recordedSequence.joinToString(" >> ") { it.name }
            tvSequence.alpha = 1f
        }
    }
    
    private fun updateUIState() {
        // Initial State
        tvSequence.text = "[ WAITING FOR INPUT ]"
    }

    private fun performHaptic() {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(40, 150))
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
        if (isRecording) toggleRecording()
    }
}
