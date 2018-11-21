package com.burhanuday.potholego.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.burhanuday.potholego.R
import com.burhanuday.potholego.utils.Constants
import kotlinx.android.synthetic.main.activity_signin.*

/**
 * Created by Burhanuddin on 27-10-2018.
 */

class Signin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar!!.hide()

        /**
         * ask user for CAMERA, LOCATION and WRITE_EXTERNAL_STORAGE permissions
         */
        checkRequiredPermissions()

        bt_signin.setOnClickListener{
            val startMain = Intent(this, MainActivity::class.java)
            startActivity(startMain)
        }
    }

    private fun checkRequiredPermissions(){
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE), Constants.PERMISSION_REQUEST_CODE)
        }
    }
}
