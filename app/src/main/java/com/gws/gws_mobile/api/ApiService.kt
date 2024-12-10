package com.gws.gws_mobile.api

import com.google.gson.JsonObject
import com.gws.gws_mobile.api.response.NewsRecomendationResponse
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.api.response.QuotesResponse
import com.gws.gws_mobile.api.response.RecommendationTagResponse
import com.gws.gws_mobile.api.response.RecommendationsResponse
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
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

    @GET("quotes")
    suspend fun getQuote(): QuotesResponse

    @Headers("Content-Type: application/json")
    @POST("/recommend")
    suspend fun postRecommendations(
        @Body requestBody: JsonObject?
    ): RecommendationTagResponse
}