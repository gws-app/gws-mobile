package com.gws.gws_mobile.api

import com.google.gson.JsonObject
import com.gws.gws_mobile.api.response.MoodResponse
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.api.response.QuotesResponse
import com.gws.gws_mobile.api.response.RecommendationTagResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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