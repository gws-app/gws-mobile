package com.gws.gws_mobile.ui.login

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gws.gws_mobile.api.config.MoodApiConfig
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.database.mood.MoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val moodDatabase: MoodDatabase = MoodDatabase.getDatabase(application)

    private val _moodList = MutableLiveData<List<Mood>>()
    val moodList: LiveData<List<Mood>> = _moodList

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchMoodHistory(userId: String) {
        viewModelScope.launch {
            try {
                val response = MoodApiConfig.createApiService().getMoodHistory(userId)
                if (response.status == "success" && response.data != null) {
                    val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val moodList = response.data.map { itMoodData ->
                        val formattedCreatedAt = try {
                            val zonedDateTime = ZonedDateTime.parse(itMoodData.created_at, inputFormatter)
                            zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).format(outputFormatter)
                        } catch (e: Exception) {
                            itMoodData.created_at
                        }

                        val localVoiceNotePath: String? = if (!itMoodData.voice_note_url.isNullOrEmpty()) {
                            downloadVoiceNote(itMoodData.voice_note_url!!)
                        } else {
                            null
                        }

                        Mood(
                            user_id = itMoodData.user_id.toString(),
                            emotion = itMoodData.emotion.toString(),
                            activities = itMoodData.activities.toString(),
                            additional_activities = "",
                            note = itMoodData.note ?: "",
                            voice_note_url = localVoiceNotePath.toString(),
                            created_at = formattedCreatedAt.toString()
                        )
                    }
                    _moodList.postValue(moodList)
                } else {
                    _moodList.postValue(emptyList())
                }
            } catch (e: Exception) {
                _moodList.postValue(emptyList())
            }
        }
    }

    private suspend fun downloadVoiceNote(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(url)
                val fileName = uri.lastPathSegment ?: "voice_note_${System.currentTimeMillis()}.mp3"
                val directory = File(getApplication<Application>().getExternalFilesDir(null), "")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 10000
                urlConnection.readTimeout = 10000
                urlConnection.connect()
                val inputStream = urlConnection.inputStream
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len = inputStream.read(buffer)
                while (len != -1) {
                    outputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
                outputStream.close()
                inputStream.close()
                file.absolutePath
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun saveToDatabase(moodList: List<Mood>) {
        viewModelScope.launch {
            moodDatabase.moodDataDao().deleteAllMoodData()
            moodList.forEach { mood ->
                moodDatabase.moodDataDao().insertMoodData(mood)
            }
        }
    }
}
