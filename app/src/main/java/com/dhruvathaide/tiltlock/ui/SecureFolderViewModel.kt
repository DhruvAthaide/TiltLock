package com.dhruvathaide.tiltlock.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvathaide.tiltlock.data.SecureFile
import com.dhruvathaide.tiltlock.data.SecureFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SecureFolderViewModel(
    private val repository: SecureFileRepository
) : ViewModel() {

    private val _files = MutableLiveData<List<SecureFile>>()
    val files: LiveData<List<SecureFile>> = _files

    fun loadFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _files.postValue(repository.getFiles())
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.importFile(uri)
            loadFiles()
        }
    }
    
    fun deleteFile(file: SecureFile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFile(file)
            loadFiles()
        }
    }
    
    // Suspend function to avoid blocking UI during decryption
    suspend fun decryptFile(file: File): File {
        return repository.decryptToFile(file)
    }
}
