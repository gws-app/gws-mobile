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

    // Fungsi untuk mengirim mood data ke API
    fun sendMoodDataToApi(moodData: MoodData) {
        // Menandakan loading
        _loading.value = true

        // Memulai coroutine untuk mengirim data
        viewModelScope.launch {
            try {
                val apiService = MoodApiConfig.createApiService()
                val response = apiService.saveMood(
                    createRequestBody(moodData.userId),
                    createRequestBody(moodData.mood),
                    createRequestBody(Gson().toJson(moodData.activities)),
                    createRequestBody(moodData.note ?: ""),
                    createFilePart(moodData.voiceNoteUrl)
                )

                // Jika sukses, tampilkan pesan sukses
                val message = if (response.code == 201) "Mood data successfully saved!" else "Failed to save mood data."
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

    // Menyimpan mood data ke dalam database lokal
    fun saveMoodData(moodData: MoodData) {
        val moodDataLocal = MoodDataLocal(
            user_id = "kirmanzz",
            emotion = moodData.mood,
            activities = moodData.activities,
            note = moodData.note,
            voice_note_url = moodData.voiceNoteUrl,
            created_at = getCurrentTimestamp()
        )

        saveMoodDataToDatabase(moodDataLocal)
        sendMoodDataToApi(moodData)
    }

    // Menyimpan data mood ke database lokal
    private fun saveMoodDataToDatabase(moodDataLocal: MoodDataLocal) {
        val db = MoodDatabase.getDatabase(getApplication())
        CoroutineScope(Dispatchers.IO).launch {
            db.moodDataDao().insertMoodData(
                Mood(
                    user_id = moodDataLocal.user_id.toString(),
                    emotion = moodDataLocal.emotion.toString(),
                    activities = Gson().toJson(moodDataLocal.activities),
                    note = moodDataLocal.note.toString(),
                    voice_note_url = moodDataLocal.voice_note_url.toString(),
                    created_at = moodDataLocal.created_at
                )
            )
        }
    }

    // Mendapatkan timestamp saat ini
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        return sdf.format(Date())
    }
}
