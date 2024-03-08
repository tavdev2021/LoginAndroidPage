package com.example.loginandroidpage.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.loginandroidpage.databinding.ActivitySaveImageBinding
import com.example.loginandroidpage.util.NetworkManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class SaveImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveImageBinding
    private  lateinit var firebaseDatabase : FirebaseDatabase
    private lateinit var databaseReference : DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    private var imageFile : File? = null
    private var fileUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        databaseReference = firebaseDatabase.reference.child("Users")

        val snackbar = Snackbar.make(binding.saveImageLayout, "No esta conectado a internet, Revise su conexion", Snackbar.LENGTH_INDEFINITE)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setBackgroundTint(Color.BLUE)

        val networkManager = NetworkManager(this)
        networkManager.observe(this){

            if (!it) {
                snackbar.show()
                binding.btnConfirmar.isClickable = false
                binding.btnOmitir.isClickable = false
            } else {
                snackbar.dismiss()
                binding.btnConfirmar.isClickable = true
                binding.btnOmitir.isClickable = true
            }
        }

        binding.ImageSelector.setOnClickListener {

            selectImage()
        }

        binding.btnConfirmar.setOnClickListener {
            if (imageFile == null) {
            Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show()
            } else {
                saveImage()
            }


        }

        binding.btnOmitir.setOnClickListener {
            omitirImage()
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {result : androidx.activity.result.ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            fileUri = data?.data
            imageFile = fileUri?.path?.let {
                File(it)
            }
            binding.ImageSelector.setImageURI(fileUri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "La tarea se cancelo", Toast.LENGTH_SHORT).show()
        }

    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent {intent ->
                startImageForResult.launch(intent)
            }
    }


            private fun saveImage(){
                val userId = firebaseAuth.currentUser?.uid
                val storageReference = firebaseStorage.reference.child("Images/${userId}")

                val uri = fileUri
                val uploadImageTask = uri?.let { storageReference.putFile(it) }

                binding.progressbarindicator.isIndeterminate = true
                binding.progressbarindicator.show()

                uploadImageTask!!.continueWithTask { task ->
                    if (!task.isSuccessful) {

                        Toast.makeText(this,"No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
                        binding.progressbarindicator.isIndeterminate = false
                        binding.progressbarindicator.hide()
                        binding.progressbarindicator.isVisible = false

                        task.exception?.let {
                            throw it
                        }
                    }
                    // Continuar con la tarea de obtener la URL de la imagen
                    storageReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // URL de la imagen almacenada en Firebase Storage
                        val downloadUri = task.result

                        // Actualizar la URL de la imagen en la base de datos en tiempo real
                        actualizarURLenDatabase(downloadUri.toString())
                        goToVerifyEmailScreen()
                    } else {
                        // Manejar el fallo de obtener la URL de la imagen
                        // ...

                        Toast.makeText(this, "No se pudo obtener la url de la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    private fun actualizarURLenDatabase(urlImage: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference

        // Actualizar la URL de la imagen en la base de datos en tiempo real
        // Por ejemplo, supongamos que tienes un nodo "usuarios" y dentro de él, un nodo para cada usuario
        val userId = firebaseAuth.currentUser?.uid // Reemplaza esto con el ID del usuario actual
        val childUpdates = mapOf<String, Any>("/Users/$userId/urlImagen" to urlImage)

        // Aplicar las actualizaciones en la base de datos en tiempo real
        databaseReference.updateChildren(childUpdates)
            .addOnSuccessListener {
                // Manejar el éxito de la actualización de la URL en la base de datos
                Toast.makeText(this, "El registro se ha completado", Toast.LENGTH_SHORT).show()
                binding.progressbarindicator.isIndeterminate = false
                binding.progressbarindicator.hide()
                binding.progressbarindicator.isVisible = false
            }
            .addOnFailureListener {
                // Manejar el fallo de la actualización de la URL en la base de datos
                Toast.makeText(this, "Fallo al actualizar la url", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToVerifyEmailScreen() {
        val intent = Intent(this, VerificationEmailActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun omitirImage() {
        val defaultImageUrl  = "https://firebasestorage.googleapis.com/v0/b/loginandroidpage.appspot.com/o/DefaultUserImage%2Fdefaultuserprofile.png?alt=media&token=7c2c659e-dec4-4c34-bb3b-256ce75bbf52"
        actualizarURLenDatabase(defaultImageUrl)
        goToVerifyEmailScreen()
    }
}