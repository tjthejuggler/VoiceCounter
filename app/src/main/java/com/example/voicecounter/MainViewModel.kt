package com.example.voicecounter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: WordRepository) : ViewModel() {

    private val _wordRecognized = MutableStateFlow<Word?>(null)
    val wordRecognized: StateFlow<Word?> = _wordRecognized

    val words: StateFlow<List<Word>> = repository.allWords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addWord(word: String) = viewModelScope.launch {
        repository.insert(Word(text = word, count = 0, backgroundColor = "#FFFFFF", textColor = "#000000"))
    }

    fun incrementWordCount(result: RecognitionResult) {
        if (result.confidence > 0.5f) {
            viewModelScope.launch {
                val currentWords = words.value
                val matchedWord = currentWords.find { result.text.contains(it.text, ignoreCase = true) }
                if (matchedWord != null) {
                    val updatedWord = matchedWord.copy(count = matchedWord.count + 1)
                    repository.update(updatedWord)
                    _wordRecognized.value = updatedWord
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
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = WordRepository(database.wordDao())
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}