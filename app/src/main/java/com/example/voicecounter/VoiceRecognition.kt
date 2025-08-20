package com.example.voicecounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VoiceRecognition(private val context: Context) {

    private val _speechRecognitionAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val speechRecognitionAvailable: StateFlow<Boolean> = _speechRecognitionAvailable

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun startListening() {
        if (!speechRecognitionAvailable.value) {
            return
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    if (isListening) {
                        startListening()
                    }
                }

                override fun onError(error: Int) {
                    if (isListening && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                        startListening()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _recognizedText.value = matches[0]
                    }
                    if (isListening) {
                        startListening()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        isListening = true
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
    }
}