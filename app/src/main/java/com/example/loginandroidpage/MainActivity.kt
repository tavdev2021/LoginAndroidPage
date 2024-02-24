package com.example.loginandroidpage

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.transition.Visibility
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.example.loginandroidpage.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //private lateinit var firebaseDatabase : FirebaseDatabase
    //private lateinit var databaseReference : DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        //firebaseDatabase = FirebaseDatabase.getInstance()
        //databaseReference = firebaseDatabase.reference.child("Users")

        val snackbar = Snackbar.make(binding.loginLayout, "No esta conectado a internet, Revise su conexion", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Aceptar", View.OnClickListener {
            snackbar.dismiss()
        })
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setBackgroundTint(Color.BLUE)

        val networkManager = NetworkManager(this)
        networkManager.observe(this){

            if (!it) {
                snackbar.show()
            } else {
                snackbar.dismiss()
            }
        }

        binding.tvRegistrar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailTextEdit.text.toString().trimEnd()
            val password = binding.passwordTextEdit.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (!email.isEmailValid()) {
                    binding.emailTextEdit.error = "El email ingresado no es valido"
                    return@setOnClickListener
                } else {

                    binding.progressbarindicator.isIndeterminate = true
                    binding.progressbarindicator.show()

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val id = firebaseAuth.currentUser?.uid
                        val intent = Intent(this, Dashboard::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Ha iniciado sesión correctamente!", Toast.LENGTH_SHORT).show()
                        finish()

                        binding.progressbarindicator.isIndeterminate = false
                        binding.progressbarindicator.hide()
                        binding.progressbarindicator.isVisible = false

                    } else {
                        Toast.makeText(this, "Los datos de acceso no son correctos", Toast.LENGTH_SHORT).show()
                        binding.emailTextEdit.text.clear()
                        binding.passwordTextEdit.text.clear()
                        binding.emailTextEdit.requestFocus()

                        binding.progressbarindicator.isIndeterminate = false
                        binding.progressbarindicator.hide()
                        binding.progressbarindicator.isVisible = false

                    }
                    }
                }
            } else {
                Toast.makeText(this, "Los campos no deben estar vacios", Toast.LENGTH_SHORT).show()
                }
        }
                    //loginUser(email, password)
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}

    /*private fun loginUser(email: String, password: String){
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    for(userSnapshot in dataSnapshot.children){
                        val userData = userSnapshot.getValue(UserData::class.java)

                        if (userData != null && userData.password == password){
                            val intent = Intent(this@MainActivity, Dashboard::class.java)
                            startActivity(intent)
                            Toast.makeText(this@MainActivity, "Ha iniciado sesión correctamente!", Toast.LENGTH_SHORT).show()
                            finish()
                            return
                        } else {
                            Toast.makeText(this@MainActivity, "Los datos de acceso son incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "El usuario no existe", Toast.LENGTH_SHORT).show()
                    binding.emailTextEdit.text.clear()
                    binding.passwordTextEdit.text.clear()
                    binding.emailTextEdit.requestFocus()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error en la Base de datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }*/



//        binding.loginButton.setOnClickListener {

//            val email = binding.emailTextEdit.text.toString()
//            val password = binding.passwordTextEdit.text.toString()
//
//            if (email.isNotEmpty() && pass.isNotEmpty()) {
//
//                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        val intent = Intent(this, Dashboard::class.java)
//                        startActivity(intent)
//                        Toast.makeText(this, "Ha iniciado sesión correctamente!", Toast.LENGTH_SHORT).show()
//                        finish()
//                    } else {
//                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
//                        binding.emailTextEdit.text.clear()
//                        binding.passwordTextEdit.text.clear()
//                        binding.emailTextEdit.requestFocus()
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Los campos no deben estar vacios", Toast.LENGTH_SHORT).show()
//            }
//        }
