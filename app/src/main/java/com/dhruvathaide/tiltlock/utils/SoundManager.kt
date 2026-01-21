package com.dhruvathaide.tiltlock.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator

class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private val toneGenerator: ToneGenerator

    private var soundIds = mutableMapOf<String, Int>()
    
    // Constants
    companion object {
        const val SOUND_SCAN = "scan"
        const val SOUND_ACCESS_GRANTED = "access_granted"
        const val SOUND_ACCESS_DENIED = "access_denied"
        const val SOUND_GLITCH = "glitch"
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    }

    fun play(soundName: String) {
        // Placeholder implementation using ToneGenerator for now, 
        // as we don't have actual raw MP3/WAV assets yet.
        // If assets existed, we would soundPool.play(soundIds[soundName]...)

        when (soundName) {
            SOUND_SCAN -> {
                 toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            }
            SOUND_ACCESS_GRANTED -> {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 500)
            }
            SOUND_ACCESS_DENIED -> {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
            }
            SOUND_GLITCH -> {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 150)
            }
        }
    }
    
    fun release() {
        soundPool.release()
        toneGenerator.release()
    }
}
