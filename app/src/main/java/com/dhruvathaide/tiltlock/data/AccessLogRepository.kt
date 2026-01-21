package com.dhruvathaide.tiltlock.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

enum class LogType {
    SUCCESS, FAILURE
}

data class LogEntry(
    val type: LogType,
    val timestamp: Long = System.currentTimeMillis()
)

class AccessLogRepository(context: Context) {
    
    private val prefs = context.getSharedPreferences("access_logs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY_LOGS = "logs"
    
    fun logEvent(type: LogType) {
        val currentLogs = getLogs().toMutableList()
        currentLogs.add(0, LogEntry(type)) // Add to top
        // Limit log size to 50
        if (currentLogs.size > 50) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        saveLogs(currentLogs)
    }
    
    fun getLogs(): List<LogEntry> {
        val json = prefs.getString(KEY_LOGS, null) ?: return emptyList()
        val type = object : TypeToken<List<LogEntry>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveLogs(logs: List<LogEntry>) {
        val json = gson.toJson(logs)
        prefs.edit().putString(KEY_LOGS, json).apply()
    }
}
