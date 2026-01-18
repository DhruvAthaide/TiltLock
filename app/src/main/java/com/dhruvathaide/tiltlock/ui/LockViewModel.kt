package com.dhruvathaide.tiltlock.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dhruvathaide.tiltlock.data.GestureRepository
import com.dhruvathaide.tiltlock.data.TiltDirection
import com.dhruvathaide.tiltlock.data.TiltSensorManager

enum class LockState {
    LOCKED,
    SUCCESS,
    ERROR
}

class LockViewModel(
    private val repository: GestureRepository,
    val sensorManager: TiltSensorManager
) : ViewModel() {

    private val _lockState = MutableLiveData<LockState>(LockState.LOCKED)
    val lockState: LiveData<LockState> = _lockState

    private val _currentInput = MutableLiveData<List<TiltDirection>>(emptyList())
    val currentInput: LiveData<List<TiltDirection>> = _currentInput

    private var targetGesture: List<TiltDirection> = emptyList()

    init {
        reloadGesture()
        sensorManager.tiltEvent.observeForever { direction ->
            handleTilt(direction)
        }
    }

    fun reloadGesture() {
        targetGesture = repository.getGesture()
        reset()
    }

    private fun handleTilt(direction: TiltDirection) {
        if (_lockState.value != LockState.LOCKED) return

        val currentList = _currentInput.value.orEmpty().toMutableList()
        currentList.add(direction)
        _currentInput.value = currentList

        checkMatch(currentList)
    }

    private fun checkMatch(input: List<TiltDirection>) {
        val index = input.size - 1
        if (index >= targetGesture.size) {
            // Should not happen if logic is correct, but safe guard
            onError()
            return
        }

        if (input[index] != targetGesture[index]) {
            // Wrong move
            onError()
            return
        }

        if (input.size == targetGesture.size) {
            // Complete match
            _lockState.value = LockState.SUCCESS
        }
    }

    private fun onError() {
        _lockState.value = LockState.ERROR
        // Reset state after a delay or let View handle animation finish then reset call
        // For now, we expect the View to call reset() after animation
    }

    fun reset() {
        _currentInput.value = emptyList()
        _lockState.value = LockState.LOCKED
    }

    override fun onCleared() {
        super.onCleared()
        // No explicit removeObserver needed for observeForever if ViewModel is dead, 
        // but good practice to stop sensor if possible. 
        // Architecture-wise, Activity controls Sensor start/stop.
    }
}
