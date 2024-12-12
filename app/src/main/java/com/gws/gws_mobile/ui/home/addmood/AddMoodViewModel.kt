package com.gws.gws_mobile.ui.home.addmood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gws.gws_mobile.api.config.MoodApiConfig
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.database.mood.MoodDatabase
import com.gws.gws_mobile.helper.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMoodViewModel(application: Application) : AndroidViewModel(application) {

    private val _apiResponse = MutableLiveData<String>()
    val apiResponse: LiveData<String> get() = _apiResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun sendMoodDataToApi(moodData: MoodData) {
        val userId = SharedPreferences.getUserId(getApplication())

        _loading.value = true

        viewModelScope.launch {
            try {
                val apiService = MoodApiConfig.createApiService()
                val response = apiService.saveMood(
                    createRequestBody(userId),
                    createRequestBody(moodData.mood),
                    createRequestBody(Gson().toJson(moodData.activities)),
                    createRequestBody(moodData.note ?: ""),
                    createFilePart(moodData.voiceNoteUrl)
                )

                val message = if (response.code == 201) "Mood data saved successfully!" else "Failed to save mood data."
                _apiResponse.value = message

            } catch (e: Exception) {
                e.printStackTrace()
                _apiResponse.value = "Error saving data. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }

    private fun createRequestBody(value: String?): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value ?: "")
    }

    private fun createFilePart(fileUrl: String?): MultipartBody.Part? {
        return fileUrl?.let {
            val file = File(it)
            val requestBody = RequestBody.create("audio/m4a".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("voice_note_url", file.name, requestBody)
        }
    }

    fun saveMoodData(moodData: MoodData) {
        val userId = SharedPreferences.getUserId(getApplication())
        val moodDataLocal = MoodDataLocal(
            user_id = userId,
            emotion = moodData.mood,
            activities = moodData.activities,
            note = moodData.note,
            voice_note_url = moodData.voiceNoteUrl,
            created_at = getCurrentTimestamp()
        )

        saveMoodDataToDatabase(moodDataLocal)
        sendMoodDataToApi(moodData)
    }

    private fun saveMoodDataToDatabase(moodDataLocal: MoodDataLocal) {
        val db = MoodDatabase.getDatabase(getApplication())
        CoroutineScope(Dispatchers.IO).launch {
            val formattedActivities = moodDataLocal.activities?.entries?.joinToString(
                prefix = "{", postfix = "}", separator = ","
            ) { (key, value) -> "$key=[${value.joinToString(",")}]" }

            db.moodDataDao().insertMoodData(
                Mood(
                    user_id = moodDataLocal.user_id.toString(),
                    emotion = moodDataLocal.emotion.toString(),
                    activities = formattedActivities.toString(),
                    note = moodDataLocal.note.toString(),
                    voice_note_url = moodDataLocal.voice_note_url.toString(),
                    created_at = moodDataLocal.created_at
                )
            )
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
