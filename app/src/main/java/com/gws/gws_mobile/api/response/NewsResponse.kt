package com.gws.gws_mobile.api.response

data class NewsResponse(
	val code: Int? = null,
	val contents: List<ContentsItem?>? = null,
	val status: String? = null
)

data class ContentsItem(
	val description: String? = null,
	val text: String? = null,
	val headline: String? = null,
	val url: String? = null
)

