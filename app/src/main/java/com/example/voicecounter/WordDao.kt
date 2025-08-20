package com.example.voicecounter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<Word>>

    @Insert
    suspend fun insert(word: Word)

    @Update
    suspend fun update(word: Word)

    @Query("DELETE FROM words")
    suspend fun deleteAll()
}