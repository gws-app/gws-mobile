package com.gws.gws_mobile.ui.insight

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.gws.gws_mobile.api.config.NewsApiConfig
import com.gws.gws_mobile.api.config.TagApiConfig
import com.gws.gws_mobile.api.response.NewsResponse
import kotlinx.coroutines.launch

class InsightViewModel : ViewModel() {

    private val _response = MutableLiveData<NewsResponse>()
    val response: LiveData<NewsResponse> get() = _response

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    /**
     * Mengambil daftar berita dari API.
     */
    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val apiResponse = NewsApiConfig.provideNewsApiConfig().getNews()
                _response.postValue(apiResponse)
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Error fetching news: ${e.message}")
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
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Error fetching recommendations: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}