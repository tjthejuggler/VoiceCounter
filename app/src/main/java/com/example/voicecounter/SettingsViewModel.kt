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
        _speakWords.value = value
        sharedPreferences.edit().putBoolean("speak_words", value).apply()
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