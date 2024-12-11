package com.gws.gws_mobile

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gws.gws_mobile.databinding.ActivityMainBinding
import com.gws.gws_mobile.helper.SharedPreferences
import com.gws.gws_mobile.ui.chatbot.ChatbotActivity
import com.gws.gws_mobile.ui.settings.SettingPreferences
import com.gws.gws_mobile.ui.settings.SettingsViewModel
import com.gws.gws_mobile.ui.settings.ViewModelFactory
import com.gws.gws_mobile.ui.settings.dataStore
import android.widget.Toast
import com.gws.gws_mobile.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get Theme Status
        val pref = SettingPreferences.getInstance(dataStore)
        val factory = ViewModelFactory(pref)
        settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        settingsViewModel.getThemeSettings().observe(this) { isDarkModeActive ->
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkModeActive) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_insight, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.fabChat.setOnClickListener {
            intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }
        val userId = SharedPreferences.getUserId(this)

        if (userId == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}