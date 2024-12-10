package com.gws.gws_mobile.ui.home.addmood

import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class AddMoodViewModel : ViewModel() {

    fun saveMoodData(moodData: MoodData): String {
        return Gson().toJson(moodData)
    }
}