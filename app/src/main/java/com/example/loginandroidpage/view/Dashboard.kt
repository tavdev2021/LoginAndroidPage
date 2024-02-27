package com.example.loginandroidpage.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.loginandroidpage.R
import com.example.loginandroidpage.databinding.ActivityDashboardBinding
import com.example.loginandroidpage.util.PreferenceHelper
import com.example.loginandroidpage.util.PreferenceHelper.defaultPrefs
import com.example.loginandroidpage.util.PreferenceHelper.set
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.system.exitProcess

class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val email = currentUser.email

            readData(uid)

        } else {
            Toast.makeText(this, "El usuario no esta autenticado", Toast.LENGTH_SHORT).show()
        }

        binding.closeButton.setOnClickListener {
            clearSessionPreference()
            firebaseAuth.signOut()
            finish()
        }
    }

    private fun readData(uid: String) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(uid).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val nombre = dataSnapshot.child("firstName").value
                val apellido = dataSnapshot.child("lastName").value
                val telefono = dataSnapshot.child("telefono").value
                val email = dataSnapshot.child("email").value
                val nombreapellido = nombre.toString() + " " + apellido.toString()

                binding.userNameLastName.text = nombreapellido
                binding.userPhone.text = telefono.toString()
                binding.userEmail.text = email.toString()

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
}





// binding.closeButton.setOnClickListener {
// val message = "Esta seguro que desea cerrar la aplicacion?"
// showCustomDialogBox(message)
// }
// }
//
// private fun showCustomDialogBox(message: String?) {
// val dialog = Dialog(this)
// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
// dialog.setCancelable(false)
// dialog.setContentView(R.layout.layout_custom_dialog)
// dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
// val tvMessage : TextView = dialog.findViewById(R.id.tvMessage)
// val btnYes : Button = dialog.findViewById(R.id.btnYes)
// val btnNo : Button = dialog.findViewById(R.id.btnNo)
//
// tvMessage.text = message
//
// btnYes.setOnClickListener {
// clearSessionPreference()
// Toast.makeText(this, "La sesion se ha cerrado", Toast.LENGTH_SHORT).show()
// firebaseAuth.signOut()
// finish()
// //exitProcess(0)
// }
//
// btnNo.setOnClickListener {
// Toast.makeText(this, "Se cancelo la salida", Toast.LENGTH_SHORT).show()
// dialog.dismiss()
// }
// dialog.show()