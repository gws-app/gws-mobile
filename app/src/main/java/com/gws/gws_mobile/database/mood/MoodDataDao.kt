package com.gws.gws_mobile.database.mood

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MoodDataDao {
    @Insert
    suspend fun insertMoodData(moodData: Mood)

    @Query("SELECT * FROM mood_data")
    suspend fun getAllMoodData(): List<Mood>

    @Query("DELETE FROM mood_data")
    suspend fun deleteAllMoodData()

    @Update
    suspend fun updateMoodData(moodData: Mood)
}