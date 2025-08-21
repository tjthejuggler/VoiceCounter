package com.example.voicecounter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: WordRepository) : ViewModel() {

    private val tts = TextToSpeech(application)
    private val sharedPreferences = application.getSharedPreferences("settings", Application.MODE_PRIVATE)

    private val _wordRecognized = MutableStateFlow<Word?>(null)
    val wordRecognized: StateFlow<Word?> = _wordRecognized

    val words: StateFlow<List<Word>> = repository.allWords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addWord(name: String, words: List<String>) = viewModelScope.launch {
        repository.insert(Word(name = name, words = words, count = 0, backgroundColor = "#FFFFFF", textColor = "#000000", confidenceThreshold = 0.5f))
    }

    fun incrementWordCount(results: List<RecognitionResult>) {
        viewModelScope.launch {
            val currentWords = words.value
            for (result in results) {
                val matchedWord = currentWords.find { word ->
                    word.words.any { result.text.contains(it, ignoreCase = true) }
                }
                if (matchedWord != null) {
                    if (result.confidence >= matchedWord.confidenceThreshold || result.confidence == -1f) {
                        val updatedWord = matchedWord.copy(count = matchedWord.count + 1)
                        repository.update(updatedWord)
                        _wordRecognized.value = updatedWord
                        if (sharedPreferences.getBoolean("speak_words", false)) {
                            tts.speak(updatedWord.name)
                        }
                        break
                    }
                }
            }
        }
    }

    fun onRecognitionComplete() {
        _wordRecognized.value = null
    }

    fun updateWord(word: Word) = viewModelScope.launch {
        repository.update(word)
    }

    fun deleteWord(word: Word) = viewModelScope.launch {
        repository.delete(word)
    }

    fun resetAllCounts() = viewModelScope.launch {
        repository.resetAllCounts()
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = WordRepository(database.wordDao())
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}