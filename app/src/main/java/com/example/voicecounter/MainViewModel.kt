package com.example.voicecounter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Word(val text: String, var count: Int)

class MainViewModel : ViewModel() {

    private val _words = MutableStateFlow(listOf(Word("Hello", 0), Word("World", 0)))
    val words: StateFlow<List<Word>> = _words

    fun addWord(word: String) {
        _words.value = _words.value + Word(word, 0)
    }

    fun incrementWordCount(recognizedText: String) {
        val updatedWords = _words.value.map { word ->
            if (recognizedText.contains(word.text, ignoreCase = true)) {
                word.copy(count = word.count + 1)
            } else {
                word
            }
        }
        _words.value = updatedWords
    }
}