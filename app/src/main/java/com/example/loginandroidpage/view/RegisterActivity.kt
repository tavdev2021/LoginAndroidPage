package com.example.loginandroidpage.view

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.loginandroidpage.util.NetworkManager
import com.example.loginandroidpage.model.UserData
import com.example.loginandroidpage.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
            goToLogin()
        }

        binding.ivBackButton.setOnClickListener {
            goToLogin()
        }

        binding.registerButton.setOnClickListener {

            val nombre = binding.nameTextEdit.editText?.text.toString().trim()
            val apellido = binding.lastnameTextEdit.editText?.text.toString().trim()
            val email = binding.emailTextEdit.editText?.text.toString().trim()
            val phone = binding.phoneTextEdit.editText?.text.toString().trim()
            val password = binding.passwordTextEdit.editText?.text.toString()
            val confirmpassword = binding.confirmpasswordTextEdit.editText?.text.toString()


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
            }else if (phone.count() < 10) {
                binding.phoneTextEdit.error = "Complete su numero a 10 digitos"
                return@setOnClickListener
            } else if (password.isEmpty()) {
                binding.passwordTextEdit.error = "La Contraseña es requerida"
                return@setOnClickListener
            } else if (password.count() < 6) {
                binding.passwordTextEdit.error = "La contraseña debe contener al menos 6 caracteres"
                return@setOnClickListener
            } else if (confirmpassword.isEmpty()) {
                binding.confirmpasswordTextEdit.error = "Debe confirmar la contraseña"
                return@setOnClickListener
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

                                firebaseAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {

                                    signupUser(nombre, apellido, phone)

                                    binding.progressbarindicator.isIndeterminate = false
                                    binding.progressbarindicator.hide()
                                    binding.progressbarindicator.isVisible = false
                                }
                                    ?.addOnFailureListener { err ->
                                    Toast.makeText(this, err.toString(), Toast.LENGTH_SHORT).show()
                                    }

                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "El correo ya se encuentra registrado en la base de datos", Toast.LENGTH_SHORT).show()
                            binding.emailTextEdit.editText!!.text.clear()
                            binding.emailTextEdit.requestFocus()
                            binding.progressbarindicator.isIndeterminate = false
                            binding.progressbarindicator.hide()
                            binding.progressbarindicator.isVisible = false
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

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun signupUser(firstName: String, lastName: String, phone: String){

                    val id = firebaseAuth.currentUser?.uid
                    val userData = UserData(firstName, lastName, phone)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(this@RegisterActivity, "El registro se ha creado exitosamente", Toast.LENGTH_SHORT).show()
                    intent = Intent(this@RegisterActivity, SaveImageActivity::class.java)
                    startActivity(intent)
                    finish()
    }

    override fun onBackPressed() {
       goToLogin()
    }
}