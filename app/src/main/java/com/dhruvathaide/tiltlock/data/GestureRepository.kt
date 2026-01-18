package com.dhruvathaide.tiltlock.data

import android.content.Context
import android.content.SharedPreferences

class GestureRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tilt_lock_prefs", Context.MODE_PRIVATE)
    private val KEY_GESTURE = "saved_gesture"

    // Default: LEFT -> LEFT -> RIGHT -> UP
    private val DEFAULT_GESTURE = "LEFT,LEFT,RIGHT,UP"

    fun saveGesture(gesture: List<TiltDirection>) {
        val gestureString = gesture.joinToString(",") { it.name }
        prefs.edit().putString(KEY_GESTURE, gestureString).apply()
    }

    fun getGesture(): List<TiltDirection> {
        val savedString = prefs.getString(KEY_GESTURE, DEFAULT_GESTURE) ?: DEFAULT_GESTURE
        if (savedString.isEmpty()) return emptyList()
        
        return try {
            savedString.split(",").map { TiltDirection.valueOf(it) }
        } catch (e: Exception) {
            // Fallback to default if corruption occurs
            DEFAULT_GESTURE.split(",").map { TiltDirection.valueOf(it) }
        }
    }
}
