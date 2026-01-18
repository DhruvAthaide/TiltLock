package com.dhruvathaide.tiltlock.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val KEYSTORE_ALIAS = "secret_tilt_key"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(inputStream: InputStream, outputStream: OutputStream) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        
        val iv = cipher.iv
        outputStream.write(iv.size)
        outputStream.write(iv)

        val buffer = ByteArray(4096)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            val output = cipher.update(buffer, 0, bytesRead)
            if (output != null) outputStream.write(output)
            bytesRead = inputStream.read(buffer)
        }
        val doFinal = cipher.doFinal()
        if (doFinal != null) outputStream.write(doFinal)
    }

    fun decrypt(inputStream: InputStream, outputStream: OutputStream) {
        val ivSize = inputStream.read()
        val iv = ByteArray(ivSize)
        inputStream.read(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        val buffer = ByteArray(4096)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            val output = cipher.update(buffer, 0, bytesRead)
            if (output != null) outputStream.write(output)
            bytesRead = inputStream.read(buffer)
        }
        val doFinal = cipher.doFinal()
        if (doFinal != null) outputStream.write(doFinal)
    }
}
