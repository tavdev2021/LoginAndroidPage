package com.example.loginandroidpage.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.loginandroidpage.R
import com.example.loginandroidpage.databinding.ActivityUserAccountBinding
import com.example.loginandroidpage.util.PreferenceHelper.defaultPrefs
import com.example.loginandroidpage.util.PreferenceHelper.set
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UserAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            readData(uid)

        } else {
            Toast.makeText(this, "El usuario no esta autenticado", Toast.LENGTH_SHORT).show()
        }

        binding.ivBackButton.setOnClickListener {
            goToMainMenu()
        }

        binding.UpdateAccountButton.setOnClickListener {
            goToUpdateProfile()
        }

        binding.ChangeEmailButton.setOnClickListener {
            Toast.makeText(this, "Esta funcion aun no esta disponible", Toast.LENGTH_SHORT).show()
        }

        binding.UpdatePasswordButton.setOnClickListener {
            Toast.makeText(this, "Esta funcion aun no esta disponible", Toast.LENGTH_SHORT).show()
        }

        binding.DeleteAccountButton.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta seguro que desea eliminar su cuenta?")
                .setCancelable(false)
                .setPositiveButton("Si, eliminar") {_,_ ->
                    binding.DeleteAcountLayout.isVisible = false
                    binding.progressbardeleteindicator.isIndeterminate = true
                    binding.progressbardeleteindicator.isVisible = true

                    if (currentUser != null) {
                        val userId = currentUser.uid

                        deleteAccountUser(userId)

                    } else {
                        binding.DeleteAccountButton.isVisible = true
                        binding.progressbardeleteindicator.isIndeterminate = false
                        binding.progressbardeleteindicator.isVisible = false
                        Toast.makeText(
                            this,
                            "Ocurrio un error al intentar eliminar la cuenta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNeutralButton("No eliminar") {_,_ ->
                    Toast.makeText(this, "Se cancelo la tarea", Toast.LENGTH_SHORT).show()
                }
                .setIcon(R.drawable.alertdiag)
                .show()
        }
    }

    private fun readData(uid: String) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val nombre = dataSnapshot.child("firstName").value
                val apellido = dataSnapshot.child("lastName").value
                val telefono = dataSnapshot.child("telefono").value
                val email = firebaseAuth.currentUser?.email
                val nombreapellido = nombre.toString() + " " + apellido.toString()
                val imageUrl = dataSnapshot.child("urlImagen").value

                binding.userNameLastName.text = nombreapellido
                binding.userPhone.text = telefono.toString()
                binding.userEmail.text = email.toString()

                Glide
                    .with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .circleCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.try_later)
                    .into(binding.ImageViewProfile)

            } else {
                Toast.makeText(this, "No existe la sesion", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Ocurrio un error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearSessionPreference() {
        val preferences = defaultPrefs(this)
        preferences["session"] = false
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToUpdateProfile() {
        val intent = Intent(this, UpdateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun deleteAccountUser(userId: String) {
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        val database = FirebaseDatabase.getInstance().getReference("Users")
        val storageImage = FirebaseStorage.getInstance().getReference("Images")

        storageImage.child(userId).delete().addOnSuccessListener {
           database.child(userId).removeValue().addOnSuccessListener {
              currentUser!!.delete().addOnSuccessListener {
                  Toast.makeText(this, "Su cuenta ha sido eliminada exitosamente", Toast.LENGTH_SHORT).show()
                  clearSessionPreference()
                  binding.progressbardeleteindicator.isIndeterminate = false
                  binding.progressbardeleteindicator.isVisible = false
                  binding.DeleteAcountLayout.isVisible = true
                  goToLogin()
              }.addOnFailureListener {
                  Toast.makeText(this, "No se pudo eliminar el registro de usuario", Toast.LENGTH_SHORT).show()
                  binding.progressbardeleteindicator.isIndeterminate = false
                  binding.progressbardeleteindicator.isVisible = false
                  binding.DeleteAcountLayout.isVisible = true
              }
           }.addOnFailureListener {
               Toast.makeText(this, "No se pudo eliminar la informacion del usuario", Toast.LENGTH_SHORT).show()
               binding.progressbardeleteindicator.isIndeterminate = false
               binding.progressbardeleteindicator.isVisible = false
               binding.DeleteAcountLayout.isVisible = true
           }
        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo eliminar la foto del usuario", Toast.LENGTH_SHORT).show()
            binding.progressbardeleteindicator.isIndeterminate = false
            binding.progressbardeleteindicator.isVisible = false
            binding.DeleteAcountLayout.isVisible = true
        }
    }
}