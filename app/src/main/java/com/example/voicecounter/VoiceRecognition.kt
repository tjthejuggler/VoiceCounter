package com.example.voicecounter

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RecognitionResult(val text: String, val confidence: Float)

class VoiceRecognition(private val context: Context) {

    private val _speechRecognitionAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(context))
    val speechRecognitionAvailable: StateFlow<Boolean> = _speechRecognitionAvailable

    private val _recognitionResult = MutableStateFlow<List<RecognitionResult>>(emptyList())
    val recognitionResult: StateFlow<List<RecognitionResult>> = _recognitionResult

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val sharedPreferences = context.getSharedPreferences("settings", Application.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    fun startListening() {
        if (!speechRecognitionAvailable.value) {
            return
        }
        val extraPartialResults = sharedPreferences.getBoolean("extra_partial_results", true)
        val extraSpeechInputCompleteSilenceLengthMillis = sharedPreferences.getInt("extra_speech_input_complete_silence_length_millis", 1000)
        val extraSpeechInputPossiblyCompleteSilenceLengthMillis = sharedPreferences.getInt("extra_speech_input_possibly_complete_silence_length_millis", 500)
        val extraMaxResults = sharedPreferences.getInt("extra_max_results", 5)

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    if (isListening) {
                        handler.postDelayed({ startListening() }, 100)
                    }
                }

                override fun onError(error: Int) {
                    if (isListening && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                        handler.postDelayed({ startListening() }, 100)
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES) ?: FloatArray(matches?.size ?: 0) { -1f }
                    _recognitionResult.value = matches?.mapIndexed { index, text ->
                        RecognitionResult(text, scores.getOrElse(index) { -1f })
                    } ?: emptyList()
                    if (matches != null) {
                        Log.d("user_said", matches.joinToString(separator = "\n"))
                    }
                    if (isListening) {
                        handler.postDelayed({ startListening() }, 100)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    if (extraPartialResults) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val scores = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES) ?: FloatArray(matches?.size ?: 0) { -1f }
                        _recognitionResult.value = matches?.mapIndexed { index, text ->
                            RecognitionResult(text, scores.getOrElse(index) { -1f })
                        } ?: emptyList()
                        if (matches != null) {
                            Log.d("user_said", matches.joinToString(separator = "\n"))
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, extraPartialResults)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, extraSpeechInputCompleteSilenceLengthMillis)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, extraSpeechInputPossiblyCompleteSilenceLengthMillis)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, extraMaxResults)

        isListening = true
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        isListening = false
        handler.removeCallbacksAndMessages(null)
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
    }
}