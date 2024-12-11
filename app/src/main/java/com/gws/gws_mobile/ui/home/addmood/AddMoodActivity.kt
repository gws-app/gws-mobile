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
import com.gws.gws_mobile.R
import com.gws.gws_mobile.api.config.MoodApiConfig
import com.gws.gws_mobile.databinding.ActivityAddMoodBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
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
        if (moodName != null) {
            println("Mood Name: $moodName")  // Menampilkan nama mood yang diterima
            Log.d("AddActivity", "Mood = $moodName")
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCollapseEmotions.setOnClickListener {
            toggleCollapseExpand(binding.llEmotionsContent, binding.btnCollapseEmotions, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseSleep.setOnClickListener {
            toggleCollapseExpand(binding.llSleepContent, binding.btnCollapseSleep, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseSocialInteraction.setOnClickListener {
            toggleCollapseExpand(binding.llSocialInteractionContent, binding.btnCollapseSocialInteraction, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseWorkStudy.setOnClickListener {
            toggleCollapseExpand(binding.llWorkStudyContent, binding.btnCollapseWorkStudy, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseFoodDrink.setOnClickListener {
            toggleCollapseExpand(binding.llFoodDrinkContent, binding.btnCollapseFoodDrink, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseNatureOutdoor.setOnClickListener {
            toggleCollapseExpand(binding.llNatureOutdoorContent, binding.btnCollapseNatureOutdoor, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseEntertainment.setOnClickListener {
            toggleCollapseExpand(binding.llEntertainmentContent, binding.btnCollapseEntertainment, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseMindfulness.setOnClickListener {
            toggleCollapseExpand(binding.llMindfulnessContent, binding.btnCollapseMindfulness, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        setupActivitySelection()

        binding.btnSave.setOnClickListener {
            val quickNote = binding.etQuickNote.text.toString()
            val userId = "kirmanzz"
            val voiceNoteUrl = audioFilePath

            // Membuat objek MoodData
            val moodData = MoodData(
                userId = userId,
                mood = moodName,
                activities = selectedActivities,
                note = quickNote,
                voiceNoteUrl = voiceNoteUrl
            )

            sendMoodDataToApi(moodData)
            finish()
        }

        binding.btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                if (checkPermissions()) {
                    startRecording()
                }
            }
        }
    }
    fun setupActivitySelection() {
        val buttons = listOf(
            // Emotions
            binding.ivHappy, binding.ivExcited, binding.ivSad, binding.ivStressed,
            binding.ivAngry, binding.ivAnxious, binding.ivRelax, binding.ivGrateful,
            binding.ivConfused, binding.ivBored,

            // Food and Drink
            binding.ivHealthyFood, binding.ivComfortFood, binding.ivSkipMeals, binding.ivBloating,
            binding.ivNewRecipe, binding.ivTooMuchSugar, binding.ivHydrated, binding.ivJunkFood,
            binding.ivHomeMade,

            // Sleep
            binding.ivSleepEarly, binding.ivGoodSleep, binding.ivMediumSleep, binding.ivOverslept,
            binding.ivInsomnia, binding.ivBadSleep,

            // Social Interaction
            binding.ivMeetFriends, binding.ivFamilyTime, binding.ivOnlineChat, binding.ivLonely,
            binding.ivConflict, binding.ivHelpedSomeone,

            // Work/Study
            binding.ivProductive, binding.ivOverwhelmed, binding.ivDeadlineStress, binding.ivBrainstorming,
            binding.ivCollaboration, binding.ivAttendMeeting, binding.ivAttendClass, binding.ivLearnNewSkill,

            // Hobbies
            binding.ivReading, binding.ivGaming, binding.ivCooking, binding.ivArts,
            binding.ivGardening, binding.ivWriting, binding.ivPhotography, binding.ivDIYCrafts,
            binding.ivLanguage, binding.ivListeningMusic,

            // Nature/Outdoor
            binding.ivFreshAir, binding.ivParkVisit, binding.ivBeach, binding.ivMountain,
            binding.ivIndoor, binding.ivCamping, binding.ivSunrise, binding.ivSunset,
            binding.ivVacation, binding.ivPicnic,

            // Entertainment
            binding.ivWatchMovie, binding.ivTheater, binding.ivOrchestra, binding.ivPlayGames,
            binding.ivLiveEvent, binding.ivConcert, binding.ivWatchSports, binding.ivReadBook,

            // Mindfulness
            binding.ivMeditation, binding.ivDeepBreathing, binding.ivJournal, binding.ivSelfCare,
            binding.ivYoga, binding.ivGratitude, binding.ivDigitalDetox, binding.ivEnjoyedSilence
        )

        // Set listener untuk semua tombol ImageButton
        buttons.forEach { button ->
            button.setOnClickListener { view ->
                val activity = view.contentDescription.toString()  // Mengambil contentDescription
                val category = getCategoryForButton(view)
                toggleActivitySelection(activity, category)

                // CHECKBOX STYLE
                toggleButtonVisualState(view)
            }
        }
    }

    // Fungsi untuk menentukan kategori berdasarkan button
    private fun getCategoryForButton(view: View): String {
        return when (view) {
            binding.ivHappy, binding.ivExcited, binding.ivSad, binding.ivStressed,
            binding.ivAngry, binding.ivAnxious, binding.ivRelax, binding.ivGrateful,
            binding.ivConfused, binding.ivBored -> "Emotions"

            binding.ivHealthyFood, binding.ivComfortFood, binding.ivSkipMeals, binding.ivBloating,
            binding.ivNewRecipe, binding.ivTooMuchSugar, binding.ivHydrated, binding.ivJunkFood,
            binding.ivHomeMade -> "Food and Drink"

            binding.ivSleepEarly, binding.ivGoodSleep, binding.ivMediumSleep, binding.ivOverslept,
            binding.ivInsomnia, binding.ivBadSleep -> "Sleep"

            binding.ivMeetFriends, binding.ivFamilyTime, binding.ivOnlineChat, binding.ivLonely,
            binding.ivConflict, binding.ivHelpedSomeone -> "Social Interaction"

            binding.ivProductive, binding.ivOverwhelmed, binding.ivDeadlineStress, binding.ivBrainstorming,
            binding.ivCollaboration, binding.ivAttendMeeting, binding.ivAttendClass, binding.ivLearnNewSkill -> "Work or Study"

            binding.ivReading, binding.ivGaming, binding.ivCooking, binding.ivArts,
            binding.ivGardening, binding.ivWriting, binding.ivPhotography, binding.ivDIYCrafts,
            binding.ivLanguage, binding.ivListeningMusic -> "Hobbies"

            binding.ivFreshAir, binding.ivParkVisit, binding.ivBeach, binding.ivMountain,
            binding.ivIndoor, binding.ivCamping, binding.ivSunrise, binding.ivSunset,
            binding.ivVacation, binding.ivPicnic -> "Nature and Outdoor"

            binding.ivWatchMovie, binding.ivTheater, binding.ivOrchestra, binding.ivPlayGames,
            binding.ivLiveEvent, binding.ivConcert, binding.ivWatchSports, binding.ivReadBook -> "Entertainment"

            binding.ivMeditation, binding.ivDeepBreathing, binding.ivJournal, binding.ivSelfCare,
            binding.ivYoga, binding.ivGratitude, binding.ivDigitalDetox, binding.ivEnjoyedSilence -> "Mindfulness"

            else -> ""
        }
    }

    private fun toggleActivitySelection(activity: String, category: String) {
        if (selectedActivities.containsKey(category)) {
            val activitiesList = selectedActivities[category]?.toMutableList() ?: mutableListOf()
            if (activitiesList.contains(activity)) {
                activitiesList.remove(activity)
                Log.d("ToogleSELEK", "HILANG")
            } else {
                activitiesList.add(activity)
                Log.d("ToogleSELEK", "$activity")
            }
            selectedActivities[category] = activitiesList
        } else {
            selectedActivities[category] = listOf(activity)
        }
    }

    private fun toggleButtonVisualState(view: View) {
        if (view is ImageButton) {
            val isSelected = selectedActivities.values.flatten().contains(view.contentDescription.toString())
            if (isSelected) {
                view.setBackgroundResource(R.drawable.selected_background)
            } else {
                view.setBackgroundResource(R.drawable.default_background)
            }
        }
    }
//    private fun saveMoodData() {
//        val selectedMood = "Happy"
//        val quickNote = binding.etQuickNote.text.toString()
//
//        val moodData = MoodData(selectedMood, quickNote, audioFilePath)
//
//        val jsonString = viewModel.saveMoodData(moodData)
//
//        Log.d("AddMoodActivity", "JSON data: $jsonString")
//        saveToFile(jsonString)
//
//        Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
//    }

    private fun saveToFile(jsonString: String) {
        val file = File(filesDir, "mood_data.json")

        try {
            val outputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(outputStream)
            writer.write(jsonString)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving data!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleCollapseExpand(contentView: View, button: View, collapseIcon: Int, expandIcon: Int) {
        if (contentView.visibility == View.GONE) {
            contentView.visibility = View.VISIBLE
            (button as ImageButton).setImageResource(collapseIcon)
        } else {
            contentView.visibility = View.GONE
            (button as ImageButton).setImageResource(expandIcon)
        }
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO),
                    200
                )
                return false
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    200
                )
                return false
            }
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
    private fun sendMoodDataToApi(moodData: MoodData) {
        val apiService = MoodApiConfig.createApiService()

        lifecycleScope.launch {
            try {
                // convert activities to json
                val activitiesJson = Gson().toJson(moodData.activities)

                val userIdRequestBody = createRequestBody(moodData.userId)
                val moodRequestBody = createRequestBody(moodData.mood)
                val activitiesRequestBody = createRequestBody(activitiesJson) // Mengonversi Map ke JSON string
                val noteRequestBody = createRequestBody(moodData.note ?: "")
//                val createdAtRequestBody = createRequestBody(moodData.createdAt)

                val voiceNotePart = createFilePart(moodData.voiceNoteUrl)

                val response = apiService.saveMood(
                    userIdRequestBody,
                    moodRequestBody,
                    activitiesRequestBody,
                    noteRequestBody,
                    voiceNotePart
//                    createdAtRequestBody
                )

                // Periksa respons dan beri feedback
                if (response.code == 201) {  // Status code 200 berarti sukses
                    Log.d("AddMoodActivity", "Mood data successfully saved to API!")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Yey, kamu udah simpen mood kamuu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("AddMoodActivity", "Yah, gagal simpen mood kamu:(: ${response.status}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Yah, gagal simpen mood kamu:(", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FormData", "userId: ${moodData.userId}")
                Log.d("FormData", "mood: ${moodData.mood}")
                Log.d("FormData", "activities: ${Gson().toJson(moodData.activities)}")
                Log.d("FormData", "note: ${moodData.note}")
//                Log.d("FormData", "createdAt: ${moodData.createdAt}")
                Log.e("AddMoodActivity", "Error saving mood data to API: ${e.localizedMessage}")
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


    override fun onBackPressed() {
        super.onBackPressed()
        mediaRecorder?.release()
        if (!isRecording) {
            deletePreviousAudioFile()
        }
        finish()
    }
}
