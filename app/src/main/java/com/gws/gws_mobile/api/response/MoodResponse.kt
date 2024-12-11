package com.gws.gws_mobile.api.response

data class MoodResponse(
	val code: Int? = null,
	val data: MoodData? = null,
	val status: String? = null
)

data class MoodData(
	val emotion: String? = null,
	val userId: String? = null,
	val voiceNoteUrl: String? = null,
	val activities: Map<String, List<String>>? = null,
)

data class MoodDataResponse(
	val user_id: String? = null,  // user_id -> userId
	val emotion: String? = null,
	val activities: Map<String, List<String>>? = null,
	val note: String? = null,  // Menambahkan note untuk menampung data catatan
	val voice_note_url: String? = null,  // voice_note_url
	val created_at: String? = null  // created_at
)

data class MoodResponseHome(
	val code: Int? = null,
	val data: List<MoodDataResponse>? = null,
	val status: String? = null
)