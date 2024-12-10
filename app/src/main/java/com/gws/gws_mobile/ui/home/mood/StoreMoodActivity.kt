package com.gws.gws_mobile.ui.home.mood

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gws.gws_mobile.R
import com.gws.gws_mobile.databinding.ActivityStoreMoodBinding
import android.view.View

class StoreMoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreMoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityStoreMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val moodIndex = intent.getIntExtra("MOOD_INDEX", -1)
        Log.d("StoreMoodActivity", "Mood Index: $moodIndex")
        val collapsibleSections = mapOf(
            binding.btnCollapseEmotions to binding.llEmotionsContent,
            binding.btnCollapseSleep to binding.llSleepContent,
            binding.btnSocialInteraction to binding.llSocialInteractionContent,
            binding.btnCollapseWorkStudy to binding.llWorkStudyContent,
            binding.btnCollapseHobbies to binding.llHobbiesContent,
            binding.btnCollapseFoodDrink to binding.llFoodDrinkContent,
            binding.btnCollapseNatureOutdoor to binding.llNatureOutdoorContent,
            binding.btnCollapseEntertainment to binding.llEntertainmentContent,
            binding.btnCollapseMindfulness to binding.llMindfulnessContent
        )

        collapsibleSections.forEach { (button, content) ->
            button.setOnClickListener {
                val isContentVisible = content.visibility == View.VISIBLE
                content.visibility = if (isContentVisible) View.GONE else View.VISIBLE

                button.setImageResource(
                    if (isContentVisible) R.drawable.ic_arrow_drop_down_24 else R.drawable.ic_arrow_drop_up_24
                )
            }
        }
    }
}