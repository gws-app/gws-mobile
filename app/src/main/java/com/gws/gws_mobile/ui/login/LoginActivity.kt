package com.gws.gws_mobile.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gws.gws_mobile.MainActivity
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.config.MoodApiConfig
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.database.mood.MoodDatabase
import com.gws.gws_mobile.helper.SharedPreferences
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUserId: EditText
    private lateinit var buttonLogin: Button
    private lateinit var moodDatabase: MoodDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        editTextUserId = findViewById(R.id.editTextUserId)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Initialize database
        moodDatabase = MoodDatabase.getDatabase(this)

        buttonLogin.setOnClickListener {
            val userId = editTextUserId.text.toString()

            if (userId.isNotEmpty()) {
                SharedPreferences.saveUserId(this, userId)

                fetchMoodHistory(userId) { moodList ->
                    saveToDatabase(moodList)

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "User ID cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchMoodHistory(userId: String, callback: (List<Mood>) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = MoodApiConfig.createApiService().getMoodHistory(userId)
                if (response.status == "success" && response.data != null) {
                    val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    val moodList = response.data.map {
                        val formattedCreatedAt = try {
                            val zonedDateTime = ZonedDateTime.parse(it.created_at, inputFormatter)
                            zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).format(outputFormatter)
                        } catch (e: Exception) {
                            Log.e("FetchMoodHistory", "Error formatting date: ${e.message}")
                            it.created_at
                        }

                        Mood(
                            user_id = it.user_id.toString(),
                            emotion = it.emotion.toString(),
                            activities = it.activities.toString(),
                            note = it.note ?: "",
                            voice_note_url = it.voice_note_url ?: "",
                            created_at = formattedCreatedAt.toString()
                        )
                    }
                    callback(moodList)
                } else {
                    Log.e("LoginActivity", "Error fetching mood history: ${response.status}")
                    callback(emptyList())
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error fetching mood history: ${e.message}")
                callback(emptyList())
            }
        }
    }

    private fun saveToDatabase(moodList: List<Mood>) {
        lifecycleScope.launch {
            moodDatabase.moodDataDao().deleteAllMoodData()
            moodList.forEach { mood ->
                moodDatabase.moodDataDao().insertMoodData(mood)
            }
            Log.d("LoginActivity", "Mood history saved to database")
        }
    }
}

