package com.example.loginandroidpage.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.loginandroidpage.R
import com.example.loginandroidpage.databinding.ActivityUpdateProfileBinding
import com.example.loginandroidpage.databinding.ActivityUserAccountBinding
import com.example.loginandroidpage.model.UserData
import com.example.loginandroidpage.util.NetworkManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var firebaseDatabase : FirebaseDatabase
    private lateinit var databaseReference : DatabaseReference
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth

    private var imageFile : File? = null
    private var fileUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        databaseReference = firebaseDatabase.reference.child("Users")

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            readData(uid)

        } else {
            Toast.makeText(this, "El usuario no esta autenticado", Toast.LENGTH_SHORT).show()
        }

        val snackbar = Snackbar.make(binding.updateLayout, "No esta conectado a internet, Revise su conexion", Snackbar.LENGTH_INDEFINITE)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setBackgroundTint(Color.BLUE)

        val networkManager = NetworkManager(this)
        networkManager.observe(this){

            if (!it) {
                snackbar.show()
                binding.updateButton.isClickable = false
            } else {
                snackbar.dismiss()
                binding.updateButton.isClickable = true
            }
        }

        binding.ImageSelector.setOnClickListener {

            selectImage()
        }

        binding.updateButton.setOnClickListener {
            if (imageFile == null) {

                val nombre = binding.nameTextEdit.editText?.text.toString().trim()
                val apellido = binding.lastnameTextEdit.editText?.text.toString().trim()
                val phone = binding.phoneTextEdit.editText?.text.toString().trim()


                if (nombre.isEmpty()) {
                    binding.nameTextEdit.error = "El Nombre es requerido"
                    return@setOnClickListener
                } else if (apellido.isEmpty()) {
                    binding.lastnameTextEdit.error = "El Apellido es requerido"
                    return@setOnClickListener
                } else if (phone.isEmpty()) {
                    binding.phoneTextEdit.error = "El Telefono es requerido"
                    return@setOnClickListener
                } else if (phone.count() < 10) {
                    binding.phoneTextEdit.error = "Complete su numero a 10 digitos"
                    return@setOnClickListener
                } else {
                            binding.progressbarindicator.isIndeterminate = true
                            binding.progressbarindicator.show()

                            updateDataWithOutPicture()

                        }
                    } else {
                val nombre = binding.nameTextEdit.editText?.text.toString().trim()
                val apellido = binding.lastnameTextEdit.editText?.text.toString().trim()
                val phone = binding.phoneTextEdit.editText?.text.toString().trim()


                if (nombre.isEmpty()) {
                    binding.nameTextEdit.error = "El Nombre es requerido"
                    return@setOnClickListener
                } else if (apellido.isEmpty()) {
                    binding.lastnameTextEdit.error = "El Apellido es requerido"
                    return@setOnClickListener
                } else if (phone.isEmpty()) {
                    binding.phoneTextEdit.error = "El Telefono es requerido"
                    return@setOnClickListener
                } else if (phone.count() < 10) {
                    binding.phoneTextEdit.error = "Complete su numero a 10 digitos"
                    return@setOnClickListener
                } else {
                    binding.progressbarindicator.isIndeterminate = true
                    binding.progressbarindicator.show()

                    updateAllData()
                        }
                }

            }

        binding.ivBackButton.setOnClickListener {
            goToUserProfile()
        }
    }

    private fun goToUserProfile() {
        val intent = Intent(this, UserAccountActivity::class.java)
        startActivity(intent)
        finish()
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


    private fun updateImage(){
        val userId = firebaseAuth.currentUser?.uid
        val storageReference = firebaseStorage.reference.child("Images/${userId}")

        val uri = fileUri
        val uploadImageTask = uri?.let { storageReference.putFile(it) }

        uploadImageTask!!.continueWithTask { task ->
            if (!task.isSuccessful) {

                Toast.makeText(this,"No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()

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
                binding.progressbarindicator.isIndeterminate = false
                binding.progressbarindicator.isVisible = false

                goToUserProfile()

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
            }
            .addOnFailureListener {
                // Manejar el fallo de la actualización de la URL en la base de datos
                Toast.makeText(this, "Fallo al actualizar la url", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAllData(){

        val nombre = binding.nameTextEdit.editText?.text.toString().trim()
        val apellido = binding.lastnameTextEdit.editText?.text.toString().trim()
        val phone = binding.phoneTextEdit.editText?.text.toString().trim()

        val id = firebaseAuth.currentUser?.uid

        val editMap = mapOf(
            "firstName" to nombre,
            "lastName" to apellido,
            "telefono" to phone
        )


        databaseReference.child(id!!).updateChildren(editMap).addOnCompleteListener { task ->

                updateImage()

        }.addOnFailureListener {
            Toast.makeText(this, "Ocurrio un error al actualizar los datos", Toast.LENGTH_SHORT).show()
            binding.progressbarindicator.isIndeterminate = false
            binding.progressbarindicator.isVisible = false
        }

    }

    private fun updateDataWithOutPicture() {

        val nombre = binding.nameTextEdit.editText?.text.toString().trim()
        val apellido = binding.lastnameTextEdit.editText?.text.toString().trim()
        val phone = binding.phoneTextEdit.editText?.text.toString().trim()

        val id = firebaseAuth.currentUser?.uid

        val editMap = mapOf(
            "firstName" to nombre,
            "lastName" to apellido,
            "telefono" to phone
        )

        databaseReference.child(id!!).updateChildren(editMap).addOnCompleteListener { task ->
            if (task.isSuccessful){
                binding.progressbarindicator.isIndeterminate = false
                binding.progressbarindicator.isVisible = false
                goToUserProfile()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ocurrio un error al actualizar los datos", Toast.LENGTH_SHORT).show()
            binding.progressbarindicator.isIndeterminate = false
            binding.progressbarindicator.isVisible = false
        }
    }

    private fun readData(uid: String) {
        val databaseReference = firebaseDatabase.reference.child("Users")

        databaseReference.child(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val nombre = dataSnapshot.child("firstName").value.toString()
                val apellido = dataSnapshot.child("lastName").value.toString()
                val telefono = dataSnapshot.child("telefono").value.toString()
                val imageUrl = dataSnapshot.child("urlImagen").value

                binding.nameTextEdit.editText!!.setText(nombre)
                binding.lastnameTextEdit.editText!!.setText(apellido)
                binding.phoneTextEdit.editText!!.setText(telefono)

                Glide
                    .with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .circleCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.try_later)
                    .into(binding.ImageSelector)

            } else {
                Toast.makeText(this, "No existe la sesion", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Ocurrio un error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
    }
}