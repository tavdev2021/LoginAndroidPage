package com.example.loginandroidpage.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.loginandroidpage.R
import com.example.loginandroidpage.databinding.ActivityMainMenuBinding
import com.example.loginandroidpage.util.PreferenceHelper
import com.example.loginandroidpage.util.PreferenceHelper.set
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class MainMenuActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainMenuBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var firebaseDataBase : FirebaseDatabase
    private lateinit var firebaseStorage : FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDataBase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        val firebaseStorageReference = firebaseStorage.reference.child("Images")
        val currentUser : FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            readData(uid)

        } else {
            Toast.makeText(this, "El usuario no esta autenticado", Toast.LENGTH_SHORT).show()
        }

        binding.cvUserProfile.setOnClickListener {
            goToUserProfile()
        }

        binding.cvOrder.setOnClickListener {
            Toast.makeText(this, "Esta funcion aun no esta disponible", Toast.LENGTH_SHORT).show()
        }

        binding.cvProducts.setOnClickListener {
            Toast.makeText(this, "Esta funcion aun no esta disponible", Toast.LENGTH_SHORT).show()
        }

        binding.cvPay.setOnClickListener {
            Toast.makeText(this, "Esta funcion aun no esta disponible", Toast.LENGTH_SHORT).show()
        }

        binding.cvSettings.setOnClickListener {
            Toast.makeText(this, "Esta funcion aun no esta disponible", Toast.LENGTH_SHORT).show()
        }

        binding.cvLogout.setOnClickListener {
            clearSessionPreference()
            firebaseAuth.signOut()
            goToLogin()
        }

        binding.ivUserProfile.setOnClickListener {
            goToUserProfile()
        }
    }

    private fun goToUserProfile() {
        intent = Intent(this, UserAccountActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearSessionPreference() {
        val preferences = PreferenceHelper.defaultPrefs(this)
        preferences["session"] = false
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun readData(uid: String) {
        val dataBaseReference = firebaseDataBase.reference.child("Users")

        dataBaseReference.child(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val nombre = dataSnapshot.child("firstName").value
                val imageUrl = dataSnapshot.child("urlImagen").value

                binding.usernameTextView.text = nombre.toString()

                Glide
                    .with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .circleCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.try_later)
                    .into(binding.ivUserProfile)

            } else {
                Toast.makeText(this, "No existe la sesion", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Ocurrio un error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }
}