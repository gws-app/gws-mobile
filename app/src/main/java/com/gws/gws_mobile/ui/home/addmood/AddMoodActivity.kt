package com.gws.gws_mobile.ui.home.addmood

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gws.gws_mobile.R
import com.gws.gws_mobile.databinding.ActivityAddMoodBinding
import com.gws.gws_mobile.databinding.ActivityStoreMoodBinding
import com.gws.gws_mobile.databinding.ContainerAddMoodBinding

class AddMoodActivity : AppCompatActivity(){
    private lateinit var binding: ActivityAddMoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityAddMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val moodIndex = intent.getIntExtra("MOOD_INDEX", -1)
        Log.d("StoreMoodActivity", "Mood Index: $moodIndex")

    }
}