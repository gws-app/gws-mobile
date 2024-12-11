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

data class MoodResponseHome(
	val code: Int? = null,
	val data: List<MoodData>? = null,
	val status: String? = null
)