package com.example.loginandroidpage.view

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.loginandroidpage.databinding.ActivityLoginBinding
import com.example.loginandroidpage.util.NetworkManager
import com.example.loginandroidpage.util.PreferenceHelper
import com.example.loginandroidpage.util.PreferenceHelper.defaultPrefs
import com.example.loginandroidpage.util.PreferenceHelper.get
import com.example.loginandroidpage.util.PreferenceHelper.set
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseDatabase : FirebaseDatabase
    private lateinit var databaseReference : DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = PreferenceHelper.defaultPrefs(this)
        if (preferences ["session", false])
            goToProfile()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("Users")

        val snackbar = Snackbar.make(binding.loginLayout, "No esta conectado a internet, Revise su conexion", Snackbar.LENGTH_INDEFINITE)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setBackgroundTint(Color.BLUE)

        val networkManager = NetworkManager(this)
        networkManager.observe(this){

            if (!it) {
                snackbar.show()
                binding.loginButton.isClickable = false
            } else {
                snackbar.dismiss()
                binding.loginButton.isClickable = true
            }
        }

        binding.tvRegistrar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvPasswordForgot.setOnClickListener {
            goToPasswordForgot()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailTextEdit.editText?.text.toString().trimEnd()
            val password = binding.passwordTextEdit.editText?.text.toString()

            if (email.isEmpty()) {
                binding.emailTextEdit.error = "El Email es requerido"
                return@setOnClickListener
            } else if (password.isEmpty()) {
                binding.passwordTextEdit.error = "La contraseña es requerida"
                return@setOnClickListener
            } else if (!email.isEmailValid()) {
                binding.emailTextEdit.error = "El email ingresado no es valido"
                return@setOnClickListener
            } else {

                    binding.progressbarindicator.isIndeterminate = true
                    binding.progressbarindicator.isVisible = true

                    loginUser()
                }
            }
        }

    private fun loginUser() {

        val email = binding.emailTextEdit.editText?.text.toString().trimEnd()
        val password = binding.passwordTextEdit.editText?.text.toString()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task->
            if (task.isSuccessful) {

                val verificationEmail = firebaseAuth.currentUser?.isEmailVerified
                if (verificationEmail == true) {

                    goToProfile()
                    Toast.makeText(this, "Ha iniciado sesion correctamente", Toast.LENGTH_SHORT).show()

                    binding.progressbarindicator.isIndeterminate = false
                    binding.progressbarindicator.isVisible = false

                } else {

                    goToVerifyEmailScreen()

                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Los datos de acceso son incorrectos", Toast.LENGTH_SHORT).show()
            binding.emailTextEdit.editText!!.text.clear()
            binding.passwordTextEdit.editText!!.text.clear()
            binding.emailTextEdit.requestFocus()
            binding.progressbarindicator.isIndeterminate = false
            binding.progressbarindicator.isVisible = false
        }
    }


    private fun goToVerifyEmailScreen() {
        val intent = Intent(this, VerificationEmailActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToProfile() {
        val intent = Intent(this, MainMenuActivity::class.java)
        createSessionPreference()
        startActivity(intent)
        finish()
    }

    private fun goToPasswordForgot() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun createSessionPreference() {
        val preferences = defaultPrefs(this)
        preferences["session"] = true
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    override fun onBackPressed() {
        exitProcess(0)
    }
}