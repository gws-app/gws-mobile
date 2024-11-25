package com.gws.gws_mobile.api

import com.gws.gws_mobile.api.response.NewsRecomendationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("getInsight.php")
    suspend fun getInsight(
        @Query("userId") userId: String
    ): NewsRecomendationResponse
}