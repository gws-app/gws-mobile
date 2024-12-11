package com.gws.gws_mobile.database.mood

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Mood::class], version = 2, exportSchema = false)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodDataDao(): MoodDataDao

    companion object {
        @Volatile
        private var INSTANCE: MoodDatabase? = null

        fun getDatabase(context: Context): MoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "mood_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}