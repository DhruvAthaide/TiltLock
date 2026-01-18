package com.dhruvathaide.tiltlock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvathaide.tiltlock.data.SecureFileRepository

class SecureFolderViewModelFactory(
    private val repository: SecureFileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecureFolderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecureFolderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
