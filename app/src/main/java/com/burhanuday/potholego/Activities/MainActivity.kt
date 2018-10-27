package com.burhanuday.potholego.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.burhanuday.potholego.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun fabClicked(view: View){
        //handle fab click
        //open camera to add new pothole

    }
}
