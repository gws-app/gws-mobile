package com.gws.gws_mobile.api.response

data class NewsRecomendationResponse(
	val news: List<NewsItem?>? = null,
	val error: Boolean? = null,
	val userId: String? = null,
	val recommendations: List<RecommendationsItem?>? = null
)

data class RecommendationsItem(
	val summary: String? = null,
	val image: String? = null,
	val id: Int? = null,
	val title: String? = null
)

data class NewsItem(
	val summary: String? = null,
	val image: String? = null,
	val id: Int? = null,
	val title: String? = null
)

