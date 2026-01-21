package com.dhruvathaide.tiltlock.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

enum class TiltDirection {
    UP, DOWN, LEFT, RIGHT
}

class TiltSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val settings = SettingsRepository(context)

    private val _tiltEvent = MutableLiveData<TiltDirection>()
    val tiltEvent: LiveData<TiltDirection> = _tiltEvent

    private val _rawGyro = MutableLiveData<Pair<Float, Float>>()
    val rawGyro: LiveData<Pair<Float, Float>> = _rawGyro

    private val DEBOUNCE_MS = 350L
    private val TILT_THRESHOLD = 2.0f
    private val RESET_THRESHOLD = 1.0f

    private var isNeutral = true
    private var lastDirection: TiltDirection? = null

    fun start() {
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type != Sensor.TYPE_GYROSCOPE) return

        val sensitivity = settings.sensitivity
        val x = event.values[0] * sensitivity
        val y = event.values[1] * sensitivity

        // Emit raw data for parallax (Always emit)
        _rawGyro.postValue(Pair(x, y))

        // State Machine for Tilt Detection
        if (isNeutral) {
            var direction: TiltDirection? = null
            
            // Check for strong tilt
            if (y < -TILT_THRESHOLD) direction = TiltDirection.LEFT
            else if (y > TILT_THRESHOLD) direction = TiltDirection.RIGHT
            else if (x < -TILT_THRESHOLD) direction = TiltDirection.UP
            else if (x > TILT_THRESHOLD) direction = TiltDirection.DOWN

            if (direction != null) {
                _tiltEvent.postValue(direction)
                isNeutral = false // ENTER TILTED STATE
                lastDirection = direction
            }
        } else {
            // In TILTED state. Wait for return to neutral (near zero).
            // We use a separate RESET_THRESHOLD.
            // Check if BOTH axes are within safe zone
            if (kotlin.math.abs(x) < RESET_THRESHOLD && kotlin.math.abs(y) < RESET_THRESHOLD) {
                isNeutral = true // RETURN TO NEUTRAL
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
