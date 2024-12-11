package com.gws.gws_mobile.ui.home.addmood

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.config.MoodApiConfig
import com.gws.gws_mobile.database.mood.Mood
import com.gws.gws_mobile.database.mood.MoodDatabase
import com.gws.gws_mobile.databinding.ActivityAddMoodBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddMoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMoodBinding
    private val viewModel = AddMoodViewModel()
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private val selectedActivities = mutableMapOf<String, List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val moodName = intent.getStringExtra("moodName")
        moodName?.let {
            Log.d("AddActivity", "Mood = $it")
        }

        setupClickListeners()
        setupActivitySelection()
    }

    private fun setupClickListeners() {
        // Back Button
        binding.btnBack.setOnClickListener { onBackPressed() }

        // Collapsing buttons
        val collapseButtons = listOf(
            binding.btnCollapseEmotions to binding.llEmotionsContent,
            binding.btnCollapseSleep to binding.llSleepContent,
            binding.btnCollapseSocialInteraction to binding.llSocialInteractionContent,
            binding.btnCollapseWorkStudy to binding.llWorkStudyContent,
            binding.btnCollapseFoodDrink to binding.llFoodDrinkContent,
            binding.btnCollapseNatureOutdoor to binding.llNatureOutdoorContent,
            binding.btnCollapseEntertainment to binding.llEntertainmentContent,
            binding.btnCollapseMindfulness to binding.llMindfulnessContent
        )

        collapseButtons.forEach { (button, contentView) ->
            button.setOnClickListener { toggleCollapseExpand(contentView, button as ImageButton) }
        }

        // Save Button
        binding.btnSave.setOnClickListener { saveMoodData() }

        // Record Button
        binding.btnRecord.setOnClickListener {
            if (isRecording) stopRecording() else if (checkPermissions()) startRecording()
        }
    }

    private fun toggleCollapseExpand(contentView: View, button: ImageButton) {
        val (expandIcon, collapseIcon) = if (contentView.visibility == View.GONE) {
            contentView.visibility = View.VISIBLE
            R.drawable.ic_arrow_drop_up_24 to R.drawable.ic_arrow_drop_down_24
        } else {
            contentView.visibility = View.GONE
            R.drawable.ic_arrow_drop_down_24 to R.drawable.ic_arrow_drop_up_24
        }
        button.setImageResource(if (contentView.visibility == View.VISIBLE) collapseIcon else expandIcon)
    }

    private fun setupActivitySelection() {
        val buttonCategoryMap = mapOf(
            "Emotions" to listOf(
                binding.ivHappy, binding.ivExcited, binding.ivSad, binding.ivStressed,
                binding.ivAngry, binding.ivAnxious, binding.ivRelax, binding.ivGrateful,
                binding.ivConfused, binding.ivBored
            ),
            "Food and Drink" to listOf(
                binding.ivHealthyFood, binding.ivComfortFood, binding.ivSkipMeals, binding.ivBloating,
                binding.ivNewRecipe, binding.ivTooMuchSugar, binding.ivHydrated, binding.ivJunkFood,
                binding.ivHomeMade
            ),
            "Sleep" to listOf(
                binding.ivSleepEarly, binding.ivGoodSleep, binding.ivMediumSleep, binding.ivOverslept,
                binding.ivInsomnia, binding.ivBadSleep
            ),
            "Social Interaction" to listOf(
                binding.ivMeetFriends, binding.ivFamilyTime, binding.ivOnlineChat, binding.ivLonely,
                binding.ivConflict, binding.ivHelpedSomeone
            ),
            "Work or Study" to listOf(
                binding.ivProductive, binding.ivOverwhelmed, binding.ivDeadlineStress, binding.ivBrainstorming,
                binding.ivCollaboration, binding.ivAttendMeeting, binding.ivAttendClass, binding.ivLearnNewSkill
            ),
            "Hobbies" to listOf(
                binding.ivReading, binding.ivGaming, binding.ivCooking, binding.ivArts,
                binding.ivGardening, binding.ivWriting, binding.ivPhotography, binding.ivDIYCrafts,
                binding.ivLanguage, binding.ivListeningMusic
            ),
            "Nature and Outdoor" to listOf(
                binding.ivFreshAir, binding.ivParkVisit, binding.ivBeach, binding.ivMountain,
                binding.ivIndoor, binding.ivCamping, binding.ivSunrise, binding.ivSunset,
                binding.ivVacation, binding.ivPicnic
            ),
            "Entertainment" to listOf(
                binding.ivWatchMovie, binding.ivTheater, binding.ivOrchestra, binding.ivPlayGames,
                binding.ivLiveEvent, binding.ivConcert, binding.ivWatchSports, binding.ivReadBook
            ),
            "Mindfulness" to listOf(
                binding.ivMeditation, binding.ivDeepBreathing, binding.ivJournal, binding.ivSelfCare,
                binding.ivYoga, binding.ivGratitude, binding.ivDigitalDetox, binding.ivEnjoyedSilence
            )
        )

        buttonCategoryMap.forEach { (category, buttons) ->
            buttons.forEach { button ->
                button.setOnClickListener {
                    val activity = button.contentDescription.toString()
                    toggleActivitySelection(activity, category)
                    toggleButtonVisualState(button)
                }
            }
        }
    }

    private fun toggleActivitySelection(activity: String, category: String) {
        val activitiesList = selectedActivities[category]?.toMutableList() ?: mutableListOf()
        if (activitiesList.contains(activity)) {
            activitiesList.remove(activity)  // This was causing the error
            Log.d("ToogleSELEK", "HILANG")
        } else {
            activitiesList.add(activity)
            Log.d("ToogleSELEK", "$activity")
        }
        selectedActivities[category] = activitiesList
    }

    private fun toggleButtonVisualState(view: View) {
        val isSelected = selectedActivities.values.flatten().contains(view.contentDescription.toString())
        view.setBackgroundResource(if (isSelected) R.drawable.selected_background else R.drawable.default_background)
    }

    private fun saveMoodData() {
        val moodData = MoodData(
            userId = "kirmanzz",
            mood = intent.getStringExtra("moodName"),
            activities = selectedActivities,
            note = binding.etQuickNote.text.toString(),
            voiceNoteUrl = audioFilePath
        )

        val moodDataLocal = MoodDataLocal(
            user_id = "kirmanzz",
            emotion = intent.getStringExtra("moodName"),
            activities = selectedActivities,
            note = binding.etQuickNote.text.toString(),
            voice_note_url = audioFilePath,
            created_at = getCurrentTimestamp()
        )

        // Menyimpan mood data ke dalam database
        saveMoodDataToDatabase(moodDataLocal)

        sendMoodDataToApi(moodData)
        saveToFile(moodDataLocal)
        finish()
    }

    private fun saveMoodDataToDatabase(moodDataLocal: MoodDataLocal) {
        // Mendapatkan instance database
        val db = MoodDatabase.getDatabase(applicationContext)

        // Menyimpan data ke dalam database menggunakan coroutine (suspend function)
        CoroutineScope(Dispatchers.IO).launch {
            db.moodDataDao().insertMoodData(
                Mood(
                    user_id = moodDataLocal.user_id.toString(),
                    emotion = moodDataLocal.emotion.toString(),
                    activities = Gson().toJson(moodDataLocal.activities), // Simpan activities sebagai JSON string
                    note = moodDataLocal.note.toString(),
                    voice_note_url = moodDataLocal.voice_note_url.toString(),
                    created_at = moodDataLocal.created_at
                )
            )
        }
    }


    private fun saveToFile(moodData: MoodDataLocal) {
        try {
            val file = File(getExternalFilesDir(null), "mood_data.json")

            // Read existing JSON data from the file, if it exists
            val jsonData = if (file.exists()) {
                file.readText()
            } else {
                "{}" // Default to an empty JSON object if file doesn't exist
            }

            // Parse the existing JSON data
            val jsonObject = Gson().fromJson(jsonData, JsonObject::class.java) ?: JsonObject()

            // Get or create the "data" array
            val dataArray = if (jsonObject.has("data")) {
                jsonObject.getAsJsonArray("data")
            } else {
                JsonArray()
            }

            // Create the JsonObject for the new mood data
            val newMoodDataJsonObject = JsonObject().apply {
                addProperty("userId", moodData.user_id)
                addProperty("mood", moodData.emotion)

                // Convert activities to JsonObject format
                val activitiesJsonObject = JsonObject()
                moodData.activities?.forEach { (category, activities) ->
                    activitiesJsonObject.add(category, Gson().toJsonTree(activities ?: JsonArray()))
                }
                add("activities", activitiesJsonObject)

                addProperty("note", moodData.note)
                addProperty("voiceNoteUrl", moodData.voice_note_url)
                addProperty("created_at", getCurrentTimestamp())
            }

            // Add the new mood data to the "data" array
            dataArray.add(newMoodDataJsonObject)

            // Update the "data" array in the root JsonObject
            jsonObject.add("data", dataArray)

            // Write the updated JSON back to the file
            file.writeText(Gson().toJson(jsonObject))
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving data to file.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun sendMoodDataToApi(moodData: MoodData) {
        val apiService = MoodApiConfig.createApiService()
        lifecycleScope.launch {
            try {
                val response = apiService.saveMood(
                    createRequestBody(moodData.userId),
                    createRequestBody(moodData.mood),
                    createRequestBody(Gson().toJson(moodData.activities)),
                    createRequestBody(moodData.note ?: ""),
                    createFilePart(moodData.voiceNoteUrl)
                )
                val message = if (response.code == 201) "Mood data successfully saved!" else "Failed to save mood data."
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AddMoodActivity", "Error saving mood data: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error saving data. Please try again.", Toast.LENGTH_SHORT).show()
                }
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

    private fun checkPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 200)
            return false
        }
        return true
    }

    private fun startRecording() {
        deletePreviousAudioFile()
        val audioFile = File(getExternalFilesDir(null), "recording_${UUID.randomUUID()}.m4a")
        audioFilePath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
                start()
                isRecording = true
                binding.btnRecord.setImageResource(R.drawable.ic_mic_100)
                Toast.makeText(this@AddMoodActivity, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@AddMoodActivity, "Failed to start recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            binding.btnRecord.setImageResource(R.drawable.ic_mic_100)
            Toast.makeText(this, "Recording saved: $audioFilePath", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePreviousAudioFile() {
        audioFilePath?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
                Log.d("AddMoodActivity", "Previous audio file deleted: $it")
            }
        }
        audioFilePath = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaRecorder?.release()
        if (!isRecording) {
            deletePreviousAudioFile()
        }
        finish()
    }
}

