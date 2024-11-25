package com.gws.gws_mobile.ui.insight.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetailViewModel : ViewModel() {
    private val _insightData = MutableLiveData<InsightData>()
    val insightData: LiveData<InsightData> get() = _insightData

    fun setInsightData(title: String, description: String, image: String) {
        _insightData.value = InsightData(title, description, image)
    }
}

data class InsightData(
    val title: String,
    val description: String,
    val image: String
)
