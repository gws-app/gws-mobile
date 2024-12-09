package com.gws.gws_mobile.api.response

data class RecommendationsResponse(
	val data: RecommendationsData? = null,
	val error: Boolean? = null
)

data class RecommendationsData(
	val image: String? = null,
	val description: String? = null,
	val title: String? = null
)

