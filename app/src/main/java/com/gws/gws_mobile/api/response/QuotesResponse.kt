package com.gws.gws_mobile.api.response

data class QuotesResponse(
	val data: Data? = null,
	val message: String? = null,
	val status: String? = null
)

data class Quote(
	val quote: String? = null,
	val author: String? = null,
	val id: String? = null
)

data class Data(
	val quote: Quote? = null
)