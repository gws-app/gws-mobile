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
import com.gws.gws_mobile.R
import com.gws.gws_mobile.databinding.ActivityAddMoodBinding
import java.io.File
import java.io.IOException
import java.util.UUID

class AddMoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMoodBinding
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private val selectedActivities = mutableMapOf<String, List<String>>()
    private lateinit var addMoodViewModel: AddMoodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        addMoodViewModel = ViewModelProvider(this).get(AddMoodViewModel::class.java)

        val moodName = intent.getStringExtra("moodName")
        moodName?.let {
            Log.d("AddActivity", "Mood = $it")
            updateEmoji(it)
        }

        setupClickListeners()
        setupActivitySelection()

        addMoodViewModel.apiResponse.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        addMoodViewModel.loading.observe(this, Observer { isLoading ->
        })
    }

    private fun updateEmoji(moodName: String) {

        val emojiResId = when (moodName) {
            "bliss" -> R.drawable.ic_45_lupbgt
            "bright" -> R.drawable.ic_45_okegpp
            "neutral" -> R.drawable.ic_45_smile
            "low" -> R.drawable.ic_45_sad
            "crumble" -> R.drawable.ic_45_sadbed
            else -> R.drawable.ic_45_smile
        }

        binding.ivEmoji.setImageResource(emojiResId)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { onBackPressed() }

        val collapseButtons = listOf(
            binding.btnCollapseEmotions to binding.llEmotionsContent,
            binding.btnCollapseSleep to binding.llSleepContent,
            binding.btnCollapseSocialInteraction to binding.llSocialInteractionContent,
            binding.btnCollapseWorkStudy to binding.llWorkStudyContent,
            binding.btnCollapseHobbies to binding.llHobbiesContent,
            binding.btnCollapseFoodDrink to binding.llFoodDrinkContent,
            binding.btnCollapseNatureOutdoor to binding.llNatureOutdoorContent,
            binding.btnCollapseEntertainment to binding.llEntertainmentContent,
            binding.btnCollapseMindfulness to binding.llMindfulnessContent
        )

        collapseButtons.forEach { (_, contentView) ->
            contentView.visibility = View.GONE
        }

        collapseButtons.forEach { (button, contentView) ->
            button.setOnClickListener { toggleCollapseExpand(contentView, button as ImageButton) }
        }

        binding.btnSave.setOnClickListener { saveMoodData() }

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

        val allActivities = selectedActivities.toMutableMap()
        if (binding.etAdditionalNote.text.isNotBlank()) {
            val additionalActivity = listOf(binding.etAdditionalNote.text.toString())
            allActivities["Additional Activities"] = additionalActivity
        }
        val moodData = MoodData(
            userId = "",
            mood = intent.getStringExtra("moodName"),
            activities = allActivities,
            additionalActivities = binding.etAdditionalNote.text.toString(),
            note = binding.etQuickNote.text.toString(),
            voiceNoteUrl = audioFilePath
        )

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
