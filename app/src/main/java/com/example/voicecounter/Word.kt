package com.example.voicecounter

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    var count: Int,
    val backgroundColor: String,
    val textColor: String
)