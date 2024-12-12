package com.gws.gws_mobile.database.mood

class MoodRepository(private val moodDataDao: MoodDataDao) {

    suspend fun getMoodHistoryFromDatabase(): List<Mood> {
        return moodDataDao.getAllMoodData()
    }

    suspend fun insertMood(mood: Mood) {
        moodDataDao.insertMoodData(mood)
    }
}
