package com.example.loginandroidpage.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.loginandroidpage.R
import com.example.loginandroidpage.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBackButton.setOnClickListener {
            goToLogin()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.sendEmailButton.setOnClickListener {
            val emailforgot = binding.emailforgotpasswordTextEdit.editText?.text.toString().trimEnd()
            if (emailforgot.isEmpty()) {
                binding.emailforgotpasswordTextEdit.error = "El Email es requerido"
                return@setOnClickListener
            } else if (!emailforgot.isEmailValid()) {
                binding.emailforgotpasswordTextEdit.error = "El email ingresado no es valido"
                return@setOnClickListener
            } else {

            firebaseAuth.sendPasswordResetEmail(emailforgot).addOnCompleteListener{
                Toast.makeText(this, "Se envio un enlace a su correo, Favor de verificar", Toast.LENGTH_SHORT).show()
                binding.emailforgotpasswordTextEdit.editText!!.text.clear()
                binding.emailforgotpasswordTextEdit.requestFocus()
            }
                .addOnFailureListener {
                    Toast.makeText(this,"Ocurrio un error, no se pudo enviar el enlace", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}