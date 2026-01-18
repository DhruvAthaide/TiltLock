package com.dhruvathaide.tiltlock.viewmodel

import androidx.lifecycle.ViewModel

class TiltLockViewModel(
    private val repo: GestureRepository
) : ViewModel() {

    private val threshold = 1.5f
    private val debounceMs = 450L

    private var gesture = repo.loadGesture()
    private var index = 0
    private var lastMoveTime = 0L

    val lockState = MutableLiveData(LockState.LOCKED)
    val progress = MutableLiveData(0f)

    fun reloadGesture() {
        gesture = repo.loadGesture()
        reset()
    }

    fun onGyro(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        if (now - lastMoveTime < debounceMs) return

        val move = detectMove(x, y) ?: return
        lastMoveTime = now

        if (move == gesture[index]) {
            index++
            progress.postValue(index / gesture.size.toFloat())

            if (index == gesture.size) {
                lockState.postValue(LockState.SUCCESS)
            }
        } else {
            error()
        }
    }

    private fun detectMove(x: Float, y: Float): GestureMove? {
        return when {
            y < -threshold -> GestureMove.LEFT
            y > threshold -> GestureMove.RIGHT
            x < -threshold -> GestureMove.UP
            x > threshold -> GestureMove.DOWN
            else -> null
        }
    }

    private fun error() {
        lockState.postValue(LockState.ERROR)
        resetDelayed()
    }

    private fun reset() {
        index = 0
        progress.postValue(0f)
    }

    private fun resetDelayed() {
        viewModelScope.launch {
            delay(700)
            reset()
            lockState.postValue(LockState.LOCKED)
        }
    }
}
