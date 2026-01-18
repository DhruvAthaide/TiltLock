package com.dhruvathaide.tiltlock.ui

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    
    private var isRecording = false
    private val recordedSequence = mutableListOf<TiltDirection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_setup)

        tvSequence = findViewById(R.id.gestureSequence)
        btnRecord = findViewById(R.id.btnRecord)
        btnSave = findViewById(R.id.btnSave)
        btnClear = findViewById(R.id.btnClear)

        sensorManager = TiltSensorManager(this)
        repository = GestureRepository(this)

        setupListeners()
        setupSensor()
    }

    private fun setupListeners() {
        btnRecord.setOnClickListener {
            toggleRecording()
        }

        btnSave.setOnClickListener {
            if (recordedSequence.isNotEmpty()) {
                repository.saveGesture(recordedSequence)
                finish() // Go back to lock screen
            }
        }

        btnClear.setOnClickListener {
            recordedSequence.clear()
            updateSequenceText()
        }
    }

    private fun toggleRecording() {
        isRecording = !isRecording
        if (isRecording) {
            btnRecord.text = "STOP RECORDING"
            recordedSequence.clear()
            updateSequenceText()
            sensorManager.start()
        } else {
            btnRecord.text = "RECORD"
            sensorManager.stop()
        }
    }

    private fun setupSensor() {
        sensorManager.tiltEvent.observe(this) { direction ->
            if (isRecording) {
                recordedSequence.add(direction)
                updateSequenceText()
                performHaptic()
            }
        }
    }

    private fun updateSequenceText() {
        if (recordedSequence.isEmpty()) {
            tvSequence.text = "..."
        } else {
            tvSequence.text = recordedSequence.joinToString(" -> ") { it.name }
        }
    }

    private fun performHaptic() {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(50, 100))
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
        if (isRecording) toggleRecording()
    }
}
