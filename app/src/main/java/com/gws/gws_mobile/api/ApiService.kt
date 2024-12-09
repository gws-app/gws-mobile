package com.gws.gws_mobile.api

import com.gws.gws_mobile.api.response.NewsRecomendationResponse
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.api.response.RecommendationsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("postInsight.php")
    suspend fun postInsight(
        @Body requestBody: Map<String, Int?>
    ): NewsRecomendationResponse

    @GET("getnews.php")
    suspend fun getNews(
        @Query("id") id: Int
    ): NewsResponse

    @GET("getrecommendations.php")
    suspend fun getRecommendations(
        @Query("id") id: Int
    ): RecommendationsResponse
}