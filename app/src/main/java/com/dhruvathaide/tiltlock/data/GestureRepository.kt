package com.dhruvathaide.tiltlock.data

import android.content.Context
import android.content.SharedPreferences

class GestureRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tilt_lock_prefs", Context.MODE_PRIVATE)
    private val KEY_GESTURE = "saved_gesture"

    fun saveGesture(gesture: List<TiltDirection>) {
        val gestureString = gesture.joinToString(",") { it.name }
        prefs.edit().putString(KEY_GESTURE, gestureString).apply()
    }

    fun getGesture(): List<TiltDirection> {
        val savedString = prefs.getString(KEY_GESTURE, null)
        if (savedString == null) return emptyList() // No custom gesture set
        
        // Backup default if needed, but for "First Launch" logic, emptyList means "Not Set"
        if (savedString.isNotEmpty()) {
             return try {
                savedString.split(",").map { TiltDirection.valueOf(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }
        return emptyList()
    }

    fun hasGesture(): Boolean {
        return prefs.contains(KEY_GESTURE)
    }
}
