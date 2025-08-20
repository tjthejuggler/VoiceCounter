package com.example.voicecounter

import kotlinx.coroutines.flow.Flow

class WordRepository(private val wordDao: WordDao) {

    val allWords: Flow<List<Word>> = wordDao.getAllWords()

    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }

    suspend fun update(word: Word) {
        wordDao.update(word)
    }

    suspend fun delete(word: Word) {
        wordDao.delete(word)
    }

    suspend fun resetAllCounts() {
        wordDao.resetAllCounts()
    }
}