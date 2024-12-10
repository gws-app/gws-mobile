package com.gws.gws_mobile.ui.home.addmood

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gws.gws_mobile.R
import com.gws.gws_mobile.databinding.ActivityAddMoodBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class AddMoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMoodBinding
    private val viewModel = AddMoodViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnCollapseEmotions.setOnClickListener {
            toggleCollapseExpand(binding.llEmotionsContent, binding.btnCollapseEmotions, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnCollapseSleep.setOnClickListener {
            toggleCollapseExpand(binding.llSleepContent, binding.btnCollapseSleep, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
        }

        binding.btnSocialInteraction.setOnClickListener {
            toggleCollapseExpand(binding.llSocialInteractionContent, binding.btnSocialInteraction, R.drawable.ic_arrow_drop_up_24, R.drawable.ic_arrow_drop_down_24)
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

        binding.btnSave.setOnClickListener {
            saveMoodData()
        }

    }
    private fun saveMoodData() {
        val selectedMood = "Happy"
        val quickNote = binding.etQuickNote.text.toString()

        val moodData = MoodData(selectedMood, quickNote)

        val jsonString = viewModel.saveMoodData(moodData)

        Log.d("AddMoodActivity", "JSON data: $jsonString")
        saveToFile(jsonString)

        Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
    }

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
}
