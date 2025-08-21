package com.example.voicecounter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : ViewModel() {

    private val sharedPreferences = application.getSharedPreferences("settings", Application.MODE_PRIVATE)

    private val _speakWords = MutableStateFlow(sharedPreferences.getBoolean("speak_words", false))
    val speakWords: StateFlow<Boolean> = _speakWords

    fun setSpeakWords(value: Boolean) {
        sharedPreferences.edit().putBoolean("speak_words", value).apply()
        _speakWords.value = value
    }

    private val _extraPartialResults = MutableStateFlow(sharedPreferences.getBoolean("extra_partial_results", true))
    val extraPartialResults: StateFlow<Boolean> = _extraPartialResults

    fun setExtraPartialResults(value: Boolean) {
        sharedPreferences.edit().putBoolean("extra_partial_results", value).apply()
        _extraPartialResults.value = value
    }

    private val _extraSpeechInputCompleteSilenceLengthMillis = MutableStateFlow(sharedPreferences.getInt("extra_speech_input_complete_silence_length_millis", 1000))
    val extraSpeechInputCompleteSilenceLengthMillis: StateFlow<Int> = _extraSpeechInputCompleteSilenceLengthMillis

    fun setExtraSpeechInputCompleteSilenceLengthMillis(value: Int) {
        sharedPreferences.edit().putInt("extra_speech_input_complete_silence_length_millis", value).apply()
        _extraSpeechInputCompleteSilenceLengthMillis.value = value
    }

    private val _extraSpeechInputPossiblyCompleteSilenceLengthMillis = MutableStateFlow(sharedPreferences.getInt("extra_speech_input_possibly_complete_silence_length_millis", 500))
    val extraSpeechInputPossiblyCompleteSilenceLengthMillis: StateFlow<Int> = _extraSpeechInputPossiblyCompleteSilenceLengthMillis

    fun setExtraSpeechInputPossiblyCompleteSilenceLengthMillis(value: Int) {
        sharedPreferences.edit().putInt("extra_speech_input_possibly_complete_silence_length_millis", value).apply()
        _extraSpeechInputPossiblyCompleteSilenceLengthMillis.value = value
    }
}

class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}