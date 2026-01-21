package com.dhruvathaide.tiltlock.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tilt_lock_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SENSITIVITY = "sensitivity"
        private const val KEY_SHOW_PATTERN = "show_pattern"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_GLITCH_ENABLED = "glitch_enabled"
    }

    // Sensitivity multiplier: 0.5 (Low) to 2.0 (High). Default 1.0
    var sensitivity: Float
        get() = prefs.getFloat(KEY_SENSITIVITY, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SENSITIVITY, value).apply()

    // Whether to show the gesture path on the lock screen
    var showPattern: Boolean
        get() = prefs.getBoolean(KEY_SHOW_PATTERN, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_PATTERN, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    var glitchEnabled: Boolean
        get() = prefs.getBoolean(KEY_GLITCH_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_GLITCH_ENABLED, value).apply()
}
