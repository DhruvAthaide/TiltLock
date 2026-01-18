package com.dhruvathaide.tiltlock.data

import android.content.Context
import android.net.Uri
import com.dhruvathaide.tiltlock.util.CryptoManager
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class SecureFile(
    val name: String,
    val file: File,
    val isImage: Boolean
)

class SecureFileRepository(private val context: Context) {
    
    private val crypto = CryptoManager() // Instance

    fun getFiles(): List<SecureFile> {
        val directory = context.filesDir
        return directory.listFiles()?.map { 
             SecureFile(it.name, it, isImageFile(it.name))
        }?.sortedByDescending { it.file.lastModified() } ?: emptyList()
    }

    fun importFile(uri: Uri) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val fileName = UUID.randomUUID().toString() + getExtension(uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        
        crypto.encrypt(inputStream, outputStream)
        
        inputStream.close()
        outputStream.close()
    }

    fun decryptToFile(encryptedFile: File): File {
        val tempFile = File.createTempFile("decrypted", encryptedFile.extension, context.cacheDir)
        val inputStream = encryptedFile.inputStream()
        val outputStream = FileOutputStream(tempFile)
        
        crypto.decrypt(inputStream, outputStream)
        
        inputStream.close()
        outputStream.close()
        return tempFile
    }

    private fun getExtension(uri: Uri): String {
        val type = context.contentResolver.getType(uri) ?: return ""
        return when {
            type.contains("image") -> ".jpg" // Simplified for demo
            type.contains("pdf") -> ".pdf"
            type.contains("video") -> ".mp4"
            else -> ".bin"
        }
    }

    private fun isImageFile(name: String): Boolean {
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")
    }
}
