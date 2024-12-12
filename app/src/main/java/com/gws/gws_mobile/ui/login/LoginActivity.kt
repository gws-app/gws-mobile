package com.gws.gws_mobile.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.gws.gws_mobile.MainActivity
import com.gws.gws_mobile.R
import com.gws.gws_mobile.helper.SharedPreferences

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextUserId: EditText
    private lateinit var buttonLogin: Button
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var progressIndicator: CircularProgressIndicator

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        editTextUserId = findViewById(R.id.editTextUserId)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressIndicator = findViewById(R.id.progressIndicator)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        loginViewModel.moodList.observe(this, { moodList -> hideProgressIndicator()

                loginViewModel.saveToDatabase(moodList)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
        })

        buttonLogin.setOnClickListener {
            val userId = editTextUserId.text.toString()
            if (userId.isNotEmpty()) {
                SharedPreferences.saveUserId(this, userId)
                showProgressIndicator()

                loginViewModel.fetchMoodHistory(userId)
            } else {
                Toast.makeText(this, "User ID tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgressIndicator() {
        progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        progressIndicator.visibility = View.GONE
    }
}
