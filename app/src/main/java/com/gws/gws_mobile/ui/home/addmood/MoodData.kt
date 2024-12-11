package com.gws.gws_mobile.ui.home.addmood

data class MoodData(
    val userId: String? = "testing",
    val mood: String?,
    val activities: Map<String, List<String>>?,
    val note: String?,
    val voiceNoteUrl: String?,
//    val createdAt: String
)

data class MoodDataLocal(
    val user_id: String? = "testing",
    val emotion: String?,
    val activities: Map<String, List<String>>?,
    val note: String?,
    val voice_note_url: String?,
    val created_at: String
)