package com.gws.gws_mobile.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gws.gws_mobile.MainActivity
import com.gws.gws_mobile.R
import com.gws.gws_mobile.helper.SharedPreferences

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUserId: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        editTextUserId = findViewById(R.id.editTextUserId)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val userId = editTextUserId.text.toString()

            if (userId.isNotEmpty()) {
                SharedPreferences.saveUserId(this, userId)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "User ID cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
