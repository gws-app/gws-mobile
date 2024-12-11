package com.gws.gws_mobile.api

import com.google.gson.JsonObject
import com.gws.gws_mobile.api.response.MoodResponse
import com.gws.gws_mobile.api.response.NewsRecomendationResponse
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.api.response.QuotesResponse
import com.gws.gws_mobile.api.response.RecommendationTagResponse
import com.gws.gws_mobile.api.response.RecommendationsResponse
import com.gws.gws_mobile.ui.home.addmood.MoodData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @Multipart
    @POST("/api/moods")
    suspend fun saveMood(
        @Part("userId") userId: RequestBody,
        @Part("mood") mood: RequestBody,
        @Part("activities") activities: RequestBody,
        @Part("note") note: RequestBody,
        @Part voiceNote: MultipartBody.Part?,
    ): MoodResponse


}