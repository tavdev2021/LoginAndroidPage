package com.example.loginandroidpage

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.loginandroidpage.databinding.ActivitySaveImageBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import java.io.File

class SaveImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaveImageBinding

    private var imageFile : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ImageSelector.setOnClickListener {
            SelectImage()
        }

        binding.btnConfirmar.setOnClickListener {
            Endesarrollo()
        }

        binding.btnOmitir.setOnClickListener {
            OmitirImage()
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result : androidx.activity.result.ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            imageFile = File(
                fileUri?.path)
            binding.ImageSelector.setImageURI(fileUri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "La tarea se cancelo", Toast.LENGTH_SHORT).show()
        }

    }

    private fun SelectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent {intent ->
                startImageForResult.launch(intent)
            }
    }


            private fun Endesarrollo(){
        val snackbar = Snackbar.make(binding.saveImageLayout, "Se esta desarrollando esta funcion", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Aceptar", View.OnClickListener {
            snackbar.dismiss()
        })
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setBackgroundTint(Color.BLUE)
        snackbar.show()
            }


    private fun OmitirImage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}