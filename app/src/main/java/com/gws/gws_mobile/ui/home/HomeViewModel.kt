package com.gws.gws_mobile.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gws.gws_mobile.api.config.MoodApiConfig
import com.gws.gws_mobile.api.config.QuotesApiConfig
import com.gws.gws_mobile.api.response.MoodData
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _quoteText = MutableLiveData<String>()
    val quoteText: LiveData<String> = _quoteText

    private val _quoteAuthor = MutableLiveData<String>()
    val quoteAuthor: LiveData<String> = _quoteAuthor

    private val _moodHistory = MutableLiveData<List<MoodData>?>()
    val moodHistory: MutableLiveData<List<MoodData>?> = _moodHistory

    fun fetchQuote() {
        viewModelScope.launch {
            try {
                val response = QuotesApiConfig.provideQuotesApiService().getQuote()
                if (response.status == "success") {
                    response.data?.quote?.let { quote ->
                        _quoteText.value = quote.quote ?: "No quote available"
                        _quoteAuthor.value = quote.author ?: "Unknown author"
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching quote: ${e.message}")
            }
        }
    }
    fun fetchMoodHistory(userId: String) {
        viewModelScope.launch {
            try {
                val response = MoodApiConfig.createApiService().getMoodHistory(userId)
                if (response.status == "success" && response.data != null) {
                    _moodHistory.value = response.data
                } else {
                    Log.e("HomeViewModel", "Error fetching mood history: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching mood history: ${e.message}")
            }
        }
    }
}
