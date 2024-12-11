package com.gws.gws_mobile.database.mood

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

data class EmotionFrequency(
    val emotion: String,
    val day: String,
    val frequency: Int
)

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

    @Query("""
        SELECT emotion, date(created_at) as day, COUNT(emotion) as frequency 
        FROM mood_data 
        WHERE date(created_at) >= date('now', '-7 days')
        GROUP BY day, emotion
        ORDER BY day, frequency DESC
    """)
    suspend fun getEmotionFrequencyLast7Days(): List<EmotionFrequency>
}
