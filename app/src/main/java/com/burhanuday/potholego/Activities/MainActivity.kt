package com.burhanuday.potholego.Activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.Toast
import com.burhanuday.potholego.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ask user for CAMERA and WRITE_EXTERNAL_STORAGE permissions
        checkRequiredPermissions()
    }

    //handle fab button click
    //open camera
    fun fabClicked(view: View){
        val openCamera = Intent(this, OpenCVCamera::class.java)
        startActivity(openCamera)
    }

    private fun checkRequiredPermissions(){
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
        }
    }
}
