package com.burhanuday.potholego.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.burhanuday.potholego.R

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
