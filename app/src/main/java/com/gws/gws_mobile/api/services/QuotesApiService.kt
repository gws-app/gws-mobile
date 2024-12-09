package com.gws.gws_mobile.api.services

import com.gws.gws_mobile.api.response.QuotesResponse
import retrofit2.http.GET

interface QuotesApiService {
    @GET("quotes")
    suspend fun getQuote(): QuotesResponse
}