package com.gws.gws_mobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gws.gws_mobile.api.config.QuotesApiConfig
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.database.mood.MoodDatabase
import com.gws.gws_mobile.database.mood.MoodRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _quoteText = MutableLiveData<String>()
    val quoteText: LiveData<String> = _quoteText

    private val _quoteAuthor = MutableLiveData<String>()
    val quoteAuthor: LiveData<String> = _quoteAuthor

    private val _moodHistory = MutableLiveData<List<Mood>>()
    val moodHistory: LiveData<List<Mood>> = _moodHistory

    private val repository: MoodRepository

    init {
        val moodDao = MoodDatabase.getDatabase(application).moodDataDao()
        repository = MoodRepository(moodDao)
    }

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
            } catch (_: Exception) {
            }
        }
    }

    fun fetchMoodHistory() {
        viewModelScope.launch {
            try {
                val data = repository.getMoodHistoryFromDatabase()
                _moodHistory.value = data
            } catch (_: Exception) {
            }
        }
    }
}
