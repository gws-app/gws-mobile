package com.gws.gws_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gws.gws_mobile.databinding.ActivityTermsAndConditionsBinding
import com.gws.gws_mobile.ui.login.LoginActivity

class TermsAndConditionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTermsAndConditionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTermsAndConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.tvTermsContent.text = getTermsAndConditionsText()

        binding.btnAccept.isEnabled = false

        binding.checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnAccept.isEnabled = isChecked
        }

        binding.btnAccept.setOnClickListener {
            if (binding.checkboxTerms.isChecked) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Please accept the Terms and Conditions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getTermsAndConditionsText(): String {
        return """Terms and Conditions

Please read these terms and conditions carefully before using this application. By accessing or using our application, you agree to be bound by the following terms:

1. Data Collection and Storage
This application collects and stores data you provide, including but not limited to personal information, activity data, or other information related to your use of our services.
All data is processed and managed in compliance with applicable laws and regulations.

2. Data Usage
The data you provide may be used to enhance our services, perform analytics, develop features, or for other lawful purposes.
We may use your data anonymously by removing identifiable information, ensuring it cannot be directly linked to you.

3. Data Security and Privacy
We are committed to safeguarding your data and employ appropriate measures to protect it from unauthorized access, misuse, or unauthorized disclosure.
However, we are not liable for security breaches caused by factors beyond our control, such as cyberattacks or unauthorized access.

4. Sharing Data with Third Parties
Your data will not be shared with third parties without your consent, except as required by law or when used in anonymized form for analysis or research purposes.

5. User Rights
You have the right to request access to, modification, or deletion of your data, subject to applicable legal requirements.
To make such a request, please contact our customer support team.

6. Changes to the Terms and Conditions
We reserve the right to modify these terms and conditions at any time without prior notice.
Any changes will be communicated through the application, and continued use of our services signifies your acceptance of the updated terms.

7. Your Consent
By using this application, you acknowledge that you have read, understood, and agreed to these terms and conditions, including the lawful use of your data as outlined herein.
If you do not agree to these terms and conditions, please discontinue using our application."""
    }
}