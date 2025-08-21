package com.example.voicecounter

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Word::class], version = 3, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "word_database"
                ).addMigrations(MIGRATION_2_3).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE words_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, words TEXT NOT NULL, count INTEGER NOT NULL, backgroundColor TEXT NOT NULL, textColor TEXT NOT NULL, confidenceThreshold REAL NOT NULL)")
                database.execSQL("INSERT INTO words_new (id, name, words, count, backgroundColor, textColor, confidenceThreshold) SELECT id, text, '[' || text || ']', count, backgroundColor, textColor, confidenceThreshold FROM words")
                database.execSQL("DROP TABLE words")
                database.execSQL("ALTER TABLE words_new RENAME TO words")
            }
        }
    }
}