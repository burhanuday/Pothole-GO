package com.burhanuday.potholego.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.burhanuday.potholego.R
import com.burhanuday.potholego.RESTApi
import com.burhanuday.potholego.models.Pothole
import com.burhanuday.potholego.utils.Constants
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Burhanuddin on 27-10-2018.
 */

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    val SYDNEY = LatLng(-33.862, 151.21)
    val MUMBAI = LatLng(19.0760, 72.8777)
    val ZOOM_LEVEL = 13f
    var restApi: RESTApi? = null
    var mapReady:Boolean = false
    var googleMap: GoogleMap? = null

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        googleMap ?: return
        with(googleMap) {
            moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(MUMBAI, ZOOM_LEVEL))
        }
        mapReady = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        //ask user for CAMERA and WRITE_EXTERNAL_STORAGE permissions
        checkRequiredPermissions()

        val mapFragment : SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        fab_new_pothole.setOnClickListener{
            //open camera to report new pothole
            val openCamera = Intent(this, OpenCVCamera::class.java)
            startActivity(openCamera)
        }

        fab_locate_me.setOnClickListener{
            //handle locate user function on map
        }

        restApi = RESTApi.create()
        val call: Call<List<Pothole>>? = restApi?.getAll()
        call!!.enqueue(object : Callback<List<Pothole>>{
            override fun onFailure(call: Call<List<Pothole>>, t: Throwable) {
                Log.i("RESPONSE", t.message)
                Toast.makeText(this@MainActivity, "There was an error retrieving the data", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Pothole>>, response: Response<List<Pothole>>) {
                val potholeList: List<Pothole>? = response.body()
                if (mapReady) {
                    for (pothole in potholeList!!) {
                        googleMap!!.addMarker(
                            com.google.android.gms.maps.model.MarkerOptions().position(
                                LatLng(
                                    pothole.location!!.lat!!,
                                    pothole.location!!.lng!!
                                )
                            )
                        )
                    }
                }
            }

        })
    }

    private fun checkRequiredPermissions(){
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.PERMISSION_REQUEST_CODE)
        }
    }
}
