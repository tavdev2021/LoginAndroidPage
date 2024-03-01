package com.example.loginandroidpage.view

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.loginandroidpage.util.NetworkManager
import com.example.loginandroidpage.model.UserData
import com.example.loginandroidpage.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseDatabase : FirebaseDatabase
    private lateinit var databaseReference : DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("Users")



        val snackbar = Snackbar.make(binding.registerLayout, "No esta conectado a internet, Revise su conexion", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Aceptar", View.OnClickListener {
            snackbar.dismiss()
        })
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setBackgroundTint(Color.BLUE)

        val networkManager = NetworkManager(this)
        networkManager.observe(this){

            if (!it) {
                snackbar.show()
                binding.registerButton.isClickable = false
            } else {
                snackbar.dismiss()
                binding.registerButton.isClickable = true
            }
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerButton.setOnClickListener {

            val nombre = binding.nameTextEdit.text.toString().trimEnd()
            val apellido = binding.lastnameTextEdit.text.toString().trimEnd()
            val email = binding.emailTextEdit.text.toString().trimEnd()
            val phone = binding.phoneTextEdit.text.toString().trimEnd()
            val password = binding.passwordTextEdit.text.toString()
            val confirmpassword = binding.confirmpasswordTextEdit.text.toString()


            if (nombre.isEmpty()) {
                binding.nameTextEdit.error = "El Nombre es requerido"
                return@setOnClickListener
            } else if (apellido.isEmpty()) {
                binding.lastnameTextEdit.error = "El Apellido es requerido"
                return@setOnClickListener
            } else if (email.isEmpty()) {
                binding.emailTextEdit.error = "El Email es requerido"
                return@setOnClickListener
            } else if (phone.isEmpty()) {
                binding.phoneTextEdit.error = "El Telefono es requerido"
                return@setOnClickListener
            } else if (password.isEmpty()) {
                binding.passwordTextEdit.error = "La Contraseña es requerida"
                return@setOnClickListener
            } else if (password.count() < 6) {
                binding.passwordTextEdit.error = "La contraseña debe contener al menos 6 caracteres"
            } else if (confirmpassword.isEmpty()) {
                binding.confirmpasswordTextEdit.error = "Debe confirmar la contraseña"
            } else {
                if (password == confirmpassword) {
                    if (!email.isEmailValid()) {
                        binding.emailTextEdit.error = "El Email ingresado no es valido"
                        return@setOnClickListener
                    } else {

                        binding.progressbarindicator.isIndeterminate = true
                        binding.progressbarindicator.show()

                        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                signupUser(nombre, apellido, email, phone, password)

                                binding.progressbarindicator.isIndeterminate = false
                                binding.progressbarindicator.hide()
                                binding.progressbarindicator.isVisible = false

                            } else {
                                Toast.makeText(this, "El email ya se encuentra registrado", Toast.LENGTH_SHORT).show()
                                binding.emailTextEdit.requestFocus()
                                binding.emailTextEdit.text.clear()
                                binding.progressbarindicator.isIndeterminate = false
                                binding.progressbarindicator.hide()
                                binding.progressbarindicator.isVisible = false
                            }
                        }
                    }
                } else {
                    binding.passwordTextEdit.error = "Las contraseñas no coinciden"
                    binding.confirmpasswordTextEdit.error = "Las contraseñas no coinciden"
                    return@setOnClickListener
                }
            }
        }
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun signupUser(firstName: String, lastName: String, email: String, phone: String, password: String){
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()){
                    val id = firebaseAuth.currentUser?.uid
                    val userData = UserData(id, firstName, lastName, email, phone, password)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(this@RegisterActivity, "El registro se ha creado exitosamente", Toast.LENGTH_SHORT).show()
                    intent = Intent(this@RegisterActivity, SaveImageActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "El correo ya esta registrado en la Base de datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@RegisterActivity, "Error en la Base de datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}



//      binding.registerButton.setOnClickListener {
//
//            val nombre = binding.nameTextEdit.text.toString()
//            val apellido = binding.lastnameTextEdit.text.toString()
//            val email = binding.emailTextEdit.text.toString()
//            val pass = binding.passwordTextEdit.text.toString()
//            val confirmpass = binding.confirmpasswordTextEdit.text.toString()
//
//            if (nombre.isNotEmpty() && apellido.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmpass.isNotEmpty()){
//
//                if (pass == confirmpass) {
//
//                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
//                        if (it.isSuccessful){
//
//                            Toast.makeText(this, "El registro se ha creado exitosamente", Toast.LENGTH_SHORT).show()
//                            val intent = Intent(this, MainActivity::class.java)
//                            startActivity(intent)
//                            finish()
//
//                        } else {
//                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                } else {
//                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "Los campos no deben estar vacios", Toast.LENGTH_SHORT).show()
//            }
//        }