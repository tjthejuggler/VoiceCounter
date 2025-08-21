package com.example.voicecounter

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeech(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
        }
    }
}