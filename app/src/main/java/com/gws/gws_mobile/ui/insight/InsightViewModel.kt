package com.gws.gws_mobile.ui.insight

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gws.gws_mobile.api.ApiConfig
import com.gws.gws_mobile.api.response.NewsRecomendationResponse
import com.gws.gws_mobile.api.response.NewsResponse
import com.gws.gws_mobile.api.response.RecommendationsResponse
import kotlinx.coroutines.launch

class InsightViewModel : ViewModel() {

    private val _response = MutableLiveData<NewsRecomendationResponse>()
    val response: LiveData<NewsRecomendationResponse> = _response

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _newsResponse = MutableLiveData<NewsResponse>()
    val newsResponse: LiveData<NewsResponse> get() = _newsResponse

    private val _recommendationResponse = MutableLiveData<RecommendationsResponse>()
    val recommendationResponse: LiveData<RecommendationsResponse> get() = _recommendationResponse

    private var isDataFetched = false

    /**
     * Memanggil fetchData hanya jika data belum pernah diambil sebelumnya.
     */
    fun fetchDataIfNeeded(requestBody: Map<String, Int?>) {
        if (!isDataFetched) {
            fetchData(requestBody)
        }
    }

    /**
     * Mengambil data dari API dan menyimpan hasilnya di LiveData.
     */
    private fun fetchData(requestBody: Map<String, Int?>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val apiResponse = ApiConfig.insightApiService().postInsight(requestBody)
                _response.postValue(apiResponse)
                isDataFetched = true
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Error fetching data: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Mengambil detail berita berdasarkan ID.
     */
    fun fetchNewsById(id: Int) {
        viewModelScope.launch {
            try {
                val apiResponse = ApiConfig.insightApiService().getNews(id)
                _newsResponse.postValue(apiResponse)
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Error fetching news by ID: ${e.message}")
            }
        }
    }

    /**
     * Mengambil detail rekomendasi berdasarkan ID.
     */
    fun fetchRecommendationById(id: Int) {
        viewModelScope.launch {
            try {
                val apiResponse = ApiConfig.insightApiService().getRecommendations(id)
                _recommendationResponse.postValue(apiResponse)
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Error fetching recommendation by ID: ${e.message}")
            }
        }
    }
}