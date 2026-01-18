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

    private val _tiltEvent = MutableLiveData<TiltDirection>()
    val tiltEvent: LiveData<TiltDirection> = _tiltEvent

    private val _rawGyro = MutableLiveData<Pair<Float, Float>>()
    val rawGyro: LiveData<Pair<Float, Float>> = _rawGyro

    private var lastTiltTime = 0L
    private val DEBOUNCE_MS = 450L
    private val THRESHOLD = 1.5f

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

        val x = event.values[0]
        val y = event.values[1]

        // Emit raw data for parallax
        _rawGyro.postValue(Pair(x, y))

        val now = System.currentTimeMillis()
        if (now - lastTiltTime < DEBOUNCE_MS) return

        var direction: TiltDirection? = null

        if (y > THRESHOLD) direction = TiltDirection.LEFT   // Negative Y is usually Left depending on orientation, but let's stick to spec. 
                                                            // Wait, Spec says: LEFT -> Negative Y, RIGHT -> Positive Y.
                                                            // Let me double check standard Android coord system.
                                                            // Standard: +Y is rotation around -X axis (top edge goes down). 
                                                            // Actually, let's stick to the prompt's request:
                                                            // LEFT -> Negative Y
                                                            // RIGHT -> Positive Y
                                                            // UP -> Negative X
                                                            // DOWN -> Positive X
        
        // My code below follows the mapping requested:
        if (y < -THRESHOLD) direction = TiltDirection.LEFT
        else if (y > THRESHOLD) direction = TiltDirection.RIGHT
        else if (x < -THRESHOLD) direction = TiltDirection.UP
        else if (x > THRESHOLD) direction = TiltDirection.DOWN

        direction?.let {
            _tiltEvent.postValue(it)
            lastTiltTime = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
