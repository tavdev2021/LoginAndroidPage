package com.example.loginandroidpage.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.loginandroidpage.databinding.ActivityVerificationEmailBinding

class VerificationEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationEmailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.okButton.setOnClickListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}