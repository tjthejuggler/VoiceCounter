package com.example.voicecounter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: WordRepository) : ViewModel() {

    val words: StateFlow<List<Word>> = repository.allWords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addWord(word: String) = viewModelScope.launch {
        repository.insert(Word(text = word, count = 0, backgroundColor = "#FFFFFF", textColor = "#000000"))
    }

    fun incrementWordCount(recognizedText: String) {
        viewModelScope.launch {
            val currentWords = words.value
            val updatedWords = currentWords.map { word ->
                if (recognizedText.contains(word.text, ignoreCase = true)) {
                    word.copy(count = word.count + 1)
                } else {
                    word
                }
            }
            updatedWords.forEach { repository.update(it) }
        }
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