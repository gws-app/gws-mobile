package com.gws.gws_mobile.api.response

data class NewsRecomendationResponse(
	val error: Boolean? = null,
	val userId: String? = null,
	val news: List<NewsItem?>? = null,
	val recommendations: List<RecommendationsItem?>? = null
)

data class NewsItem(
	val summary: String? = null,
	val image: String? = null,
	val title: String? = null
)

data class RecommendationsItem(
	val summary: String? = null,
	val image: String? = null,
	val title: String? = null
)

