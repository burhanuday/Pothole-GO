package com.burhanuday.potholego.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.burhanuday.potholego.R
import com.burhanuday.potholego.RESTApi
import com.burhanuday.potholego.models.LocationHolder
import com.burhanuday.potholego.models.Pothole
import com.burhanuday.potholego.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Burhanuddin on 27-10-2018.
 */

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val MUMBAI = LatLng(19.0760, 72.8777)
    private val ZOOM_LEVEL = 15f
    private val MINIMUM_ACCURACY = 60
    var restApi: RESTApi? = null
    private var mapReady:Boolean = false
    var googleMap: GoogleMap? = null
    var lastKnownLocation: Location?=null
    private var potholeQuerySent = false
    var didInitialZoom: Boolean = false
    var reachedMinimumAccuracy:Boolean = false

    /**
     *  Callback for when map is ready
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        googleMap ?: return
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        mapReady = true
        googleMap.setOnMyLocationChangeListener(object : GoogleMap.OnMyLocationChangeListener{
            override fun onMyLocationChange(location: Location?) {
                //Toast.makeText(this@MainActivity, "You are at ${location.toString()}", Toast.LENGTH_SHORT).show()
                LocationHolder.LATITUDE = location!!.latitude.toString()
                LocationHolder.LONGITUDE = location.longitude.toString()
                if (location.hasAccuracy() && location.accuracy<=MINIMUM_ACCURACY){
                    Toast.makeText(baseContext, "You are at $location", Toast.LENGTH_SHORT).show()
                    lastKnownLocation = location
                    reachedMinimumAccuracy = true
                    if (!potholeQuerySent){
                        getNearbyPotholes(location)
                        potholeQuerySent = true
                    }

                }
                if (!didInitialZoom){
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
                    didInitialZoom = true
                }

            }

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()

        /**
         * ask user for CAMERA, LOCATION and WRITE_EXTERNAL_STORAGE permissions
         */
        checkRequiredPermissions()

        val mapFragment : SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        /**
         * Open camera to report new pothole
         */
        fab_new_pothole.setOnClickListener{
            val openCamera = Intent(this, OpenCVCamera::class.java)
            startActivity(openCamera)
        }

        /**
         * create instance of api
         */
        restApi = RESTApi.create()

    }

    private fun getNearbyPotholes(location: Location){
        val call: Call<List<Pothole>>? = restApi?.getNearbyPotholes(location.latitude, location.longitude)
        call!!.enqueue(object : Callback<List<Pothole>>{
            override fun onFailure(call: Call<List<Pothole>>, t: Throwable) {
                Log.i("RESPONSE", t.message)
                Toast.makeText(this@MainActivity, "There was an error retrieving the data: " + t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Pothole>>, response: Response<List<Pothole>>) {
                val potholeList: List<Pothole>? = response.body()
                if (potholeList!=null){
                    addMarkersOnMap(potholeList)
                }else{
                    Log.d("MAINACTIVITY", "pothole list empty")
                }

            }

        })
    }

    /**
     * drop pins wherever potholes are located
     */
    private fun addMarkersOnMap(markers: List<Pothole>){
        if (mapReady) {
            for (pothole in markers) {
                googleMap!!.addMarker(MarkerOptions().position(LatLng(pothole.location!!.lat!!, pothole.location!!.lng!!)))
            }
        }
    }


    private fun checkRequiredPermissions(){
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE), Constants.PERMISSION_REQUEST_CODE)
        }
    }
}
