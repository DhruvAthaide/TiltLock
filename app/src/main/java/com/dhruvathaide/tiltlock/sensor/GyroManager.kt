package com.dhruvathaide.tiltlock.sensor

import android.content.Context
import android.hardware.SensorEventListener

class GyroManager(
    context: Context,
    private val callback: (Float, Float) -> Unit
) : SensorEventListener {

    private val sm =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyro =
        sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun start() {
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        callback(event.values[0], event.values[1])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
