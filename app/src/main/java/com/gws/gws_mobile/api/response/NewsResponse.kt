package com.gws.gws_mobile.api.response

data class NewsResponse(
	val data: NewsData? = null,
	val error: Boolean? = null
)

data class NewsData(
	val image: String? = null,
	val description: String? = null,
	val title: String? = null
)

