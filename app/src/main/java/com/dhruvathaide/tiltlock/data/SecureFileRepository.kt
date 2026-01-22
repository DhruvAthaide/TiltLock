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
    
    private val crypto = CryptoManager()
    private val metadataFile = File(context.filesDir, "metadata.json")
    private val gson = com.google.gson.Gson() // Assuming Gson is available, or use JSONObject
    // Fallback if Gson isn't in dependencies: Use simple JSON
    
    fun getFiles(): List<SecureFile> {
        val directory = context.filesDir
        val metadata = loadMetadata()
        
        return directory.listFiles()
            ?.filter { it.name != "metadata.json" && !it.name.endsWith(".tmp") }
            ?.map { file ->
                val originalName = metadata[file.name] ?: file.name
                SecureFile(
                    name = originalName,
                    file = file,
                    isImage = isImageFile(originalName)
                )
            }?.sortedByDescending { it.file.lastModified() } ?: emptyList()
    }

    fun importFile(uri: Uri) {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val originalName = if (nameIndex != null && nameIndex >= 0) {
            returnCursor.getString(nameIndex)
        } else {
            "Unknown_File" + getExtension(uri)
        }
        returnCursor?.close()

        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        // Use a random UUID for the storage filename to be safe/secure
        // But keep extension for some basic recovery if metadata is lost? 
        // Better to obfuscate.
        val storageName = UUID.randomUUID().toString()
        val file = File(context.filesDir, storageName)
        
        try {
            val outputStream = FileOutputStream(file)
            crypto.encrypt(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
            
            // Save metadata
            val metadata = loadMetadata().toMutableMap()
            metadata[storageName] = originalName
            saveMetadata(metadata)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Cleanup
            if (file.exists()) file.delete()
        }
    }

    fun decryptToFile(encryptedFile: File): File {
        // Recover extension from metadata to create temp file with correct extension
        val metadata = loadMetadata()
        val originalName = metadata[encryptedFile.name] ?: encryptedFile.name
        val extension = if (originalName.contains(".")) {
             "." + originalName.substringAfterLast(".")
        } else ".tmp"

        val tempFile = File.createTempFile("decrypted_", extension, context.cacheDir)
        val inputStream = encryptedFile.inputStream()
        val outputStream = FileOutputStream(tempFile)
        
        crypto.decrypt(inputStream, outputStream)
        
        inputStream.close()
        outputStream.close()
        return tempFile
    }

    private fun getExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return if (mimeType != null) {
            val ext = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (ext != null) ".$ext" else ""
        } else {
            ""
        }
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg") || lower.endsWith(".webp")
    }

    fun deleteFile(secureFile: SecureFile) {
        // Delete physical file
        if (secureFile.file.exists()) {
            secureFile.file.delete()
        }
        
        // Remove from metadata
        val metadata = loadMetadata().toMutableMap()
        if (metadata.containsKey(secureFile.file.name)) {
            metadata.remove(secureFile.file.name)
            saveMetadata(metadata)
        }
    }

    private fun loadMetadata(): Map<String, String> {
        if (!metadataFile.exists()) return emptyMap()
        return try {
            val content = metadataFile.readText()
            val typeToken = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
            gson.fromJson(content, typeToken) ?: emptyMap()
        } catch (e: Exception) {
            // Fallback to manual JSON parsing if Gson fails or file corrupt
            try {
               val json = org.json.JSONObject(metadataFile.readText())
               val map = mutableMapOf<String, String>()
               val keys = json.keys()
               while(keys.hasNext()) {
                   val key = keys.next()
                   map[key] = json.getString(key)
               }
               map
            } catch (e2: Exception) {
                emptyMap()
            }
        }
    }

    private fun saveMetadata(map: Map<String, String>) {
        try {
            val json = gson.toJson(map)
            metadataFile.writeText(json)
        } catch (e: Exception) {
             // Fallback
             val jsonObject = org.json.JSONObject(map as Map<*, *>)
            metadataFile.writeText(jsonObject.toString())
        }
    }
}
