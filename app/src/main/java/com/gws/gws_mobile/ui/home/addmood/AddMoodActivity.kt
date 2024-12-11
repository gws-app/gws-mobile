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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private val selectedActivities = mutableMapOf<String, List<String>>()

    // ViewModel
    private lateinit var addMoodViewModel: AddMoodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Inisialisasi ViewModel
        addMoodViewModel = ViewModelProvider(this).get(AddMoodViewModel::class.java)

        val moodName = intent.getStringExtra("moodName")
        moodName?.let {
            Log.d("AddActivity", "Mood = $it")
        }

        setupClickListeners()
        setupActivitySelection()

        // Observer untuk respons dari API
        addMoodViewModel.apiResponse.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        // Observer untuk status loading
        addMoodViewModel.loading.observe(this, Observer { isLoading ->
            // Tampilkan atau sembunyikan progress bar (jika ada)
        })
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
            activitiesList.remove(activity)
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

        // Mengirim data mood ke ViewModel untuk diproses
        addMoodViewModel.saveMoodData(moodData)

        finish()
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
