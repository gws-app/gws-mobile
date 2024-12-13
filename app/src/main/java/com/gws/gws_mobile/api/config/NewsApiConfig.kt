package com.gws.gws_mobile.api.config

import com.gws.gws_mobile.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsApiConfig {
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private fun createRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(createLoggingInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://core-backend-345634152468.asia-southeast2.run.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun provideNewsApiConfig(): ApiService {
        return createRetrofit().create(ApiService::class.java)
    }
}
