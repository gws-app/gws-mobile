package com.gws.gws_mobile.database.mood

import androidx.room.Entity
import androidx.room.PrimaryKey

    @Entity(tableName = "mood_data")
    data class Mood(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val user_id: String,
        val emotion: String,
        val activities: String,
        val additional_activities: String?,
        val note: String,
        val voice_note_url: String,
        val created_at: String
    )