package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.model.AppLanguage
import java.util.Locale

class VoiceTtsHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceTtsHelper", "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale("en", "IN")
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized || tts == null) return

        try {
            val locale = when (language) {
                AppLanguage.HINDI -> Locale("hi", "IN")
                AppLanguage.MARATHI -> Locale("mr", "IN")
                AppLanguage.ENGLISH -> Locale("en", "IN")
            }

            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "saathi_speech_id")
        } catch (e: Exception) {
            Log.e("VoiceTtsHelper", "Error speaking text", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceTtsHelper", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceTtsHelper", "Error shutting down TTS", e)
        }
    }
}
