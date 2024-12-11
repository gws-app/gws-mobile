package com.gws.gws_mobile.api

import com.google.gson.JsonObject
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.api.response.QuotesResponse
import com.gws.gws_mobile.api.response.RecommendationTagResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @GET("recommendation")
    suspend fun getNews(): NewsResponse

    @GET("quotes")
    suspend fun getQuote(): QuotesResponse

    @Headers("Content-Type: application/json")
    @POST("/recommend")
    suspend fun postRecommendations(
        @Body requestBody: JsonObject?
    ): RecommendationTagResponse
}