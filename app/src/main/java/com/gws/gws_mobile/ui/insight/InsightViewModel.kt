package com.gws.gws_mobile.ui.insight

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.gws.gws_mobile.api.config.NewsApiConfig
import com.gws.gws_mobile.api.config.TagApiConfig
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.database.mood.EmotionFrequency
import com.gws.gws_mobile.database.mood.MoodDataDao
import com.gws.gws_mobile.database.mood.MoodDatabase
import kotlinx.coroutines.launch

class InsightViewModel(application: Application) : AndroidViewModel(application) {

    private val _response = MutableLiveData<NewsResponse>()
    val response: LiveData<NewsResponse> get() = _response

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    private val moodDataDao: MoodDataDao = MoodDatabase.getDatabase(application).moodDataDao()

    /**
     * Mengambil daftar berita dari API.
     */
    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val apiResponse = NewsApiConfig.provideNewsApiConfig().getNews()
                _response.postValue(apiResponse)
            } catch (_: Exception) {
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Mengambil rekomendasi berdasarkan data activities.
     */
    fun fetchRecommendationTag(activities: JsonObject) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val apiResponse = TagApiConfig.TagApiConfig().postRecommendations(activities)
                _tags.postValue((apiResponse.recommendations ?: emptyList()) as List<String>?)
            } catch (_: Exception) {
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Mengambil emosi yang paling sering muncul per hari selama 7 hari terakhir.
     */
    suspend fun getMostFrequentEmotionPerDayLast7Days(): List<EmotionFrequency> {
        val allEmotionData = moodDataDao.getEmotionFrequencyLast7Days()

        val groupedByDay = allEmotionData.groupBy { it.day }

        val mostFrequentEmotions = mutableListOf<EmotionFrequency>()

        for ((day, emotions) in groupedByDay) {
            val mostFrequentEmotion = emotions.maxByOrNull { it.frequency }
            if (mostFrequentEmotion != null) {
                mostFrequentEmotions.add(mostFrequentEmotion)
            }
        }

        return mostFrequentEmotions.take(7)
    }
}
