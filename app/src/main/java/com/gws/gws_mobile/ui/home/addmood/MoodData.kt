package com.gws.gws_mobile.ui.home.addmood

data class MoodData(
    val userId: String? = "testing",
    val mood: String?,
    val activities: Map<String, List<String>>?,
    val note: String?,
    val voiceNoteUrl: String?,
//    val createdAt: String
)