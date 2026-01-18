package com.dhruvathaide.tiltlock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvathaide.tiltlock.data.GestureRepository
import com.dhruvathaide.tiltlock.data.TiltSensorManager

class LockViewModelFactory(
    private val repository: GestureRepository,
    private val sensorManager: TiltSensorManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LockViewModel(repository, sensorManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
