package com.burhanuday.potholego.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.burhanuday.potholego.ApiClient
import com.burhanuday.potholego.ApiService
import com.burhanuday.potholego.BuildConfig
import com.burhanuday.potholego.R
import com.burhanuday.potholego.models.LocationHolder
import com.burhanuday.potholego.models.Pothole
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
    private val ZOOM_LEVEL = 15f
    private val MINIMUM_ACCURACY = 45
    private var apiService:ApiService? = null
    private var mapReady:Boolean = false
    var googleMap: GoogleMap? = null
    private var potholeQuerySent = false
    var didInitialZoom: Boolean = false
    private var lastKnownLocation: Location? = null
    var potholeList: List<Pothole> = ArrayList()
    var mProgressDialog: ProgressDialog? = null

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
        //showProgressDialog()
        googleMap.setOnMyLocationChangeListener(object : GoogleMap.OnMyLocationChangeListener{
            override fun onMyLocationChange(location: Location?) {
                if (location!!.hasAccuracy() && location.accuracy<=MINIMUM_ACCURACY){
                    hideProgressDialog()
                    LocationHolder.LATITUDE = location.latitude.toString()
                    LocationHolder.LONGITUDE = location.longitude.toString()
                    lastKnownLocation = location
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val mapFragment : SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        /**
         * Open camera to report new pothole
         */
        fab_new_pothole.setOnClickListener{
            val openCamera = Intent(this, CameraActivity::class.java)
            startActivity(openCamera)
        }

        /**
         * create instance of api
         */
        val sharedPreferences = getSharedPreferences("com.burhanuday.potholego", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "")
        Log.i("mainactivity", token)
        apiService = ApiClient.getInstance(token).create(ApiService::class.java)

    }

    private fun getNearbyPotholes(location: Location){
        val call: Call<List<Pothole>>? = apiService?.getNearbyPotholes(location.latitude, location.longitude)
        call!!.enqueue(object : Callback<List<Pothole>>{
            override fun onFailure(call: Call<List<Pothole>>, t: Throwable) {
                Log.i("RESPONSE", t.message)
                Toast.makeText(this@MainActivity, "There was an error retrieving the data: " + t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Pothole>>, response: Response<List<Pothole>>) {
                potholeList = response.body()!!
                googleMap!!.clear()
                if (potholeList.isNotEmpty()){
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

    override fun onResume() {
        super.onResume()
        if (lastKnownLocation!=null){
            getNearbyPotholes(lastKnownLocation!!)
        }
    }

    private fun showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = ProgressDialog.show(this, "Searching", "Getting accurate location...", true)
        }
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog(){
        if (mProgressDialog!=null && mProgressDialog!!.isShowing){
            mProgressDialog!!.hide()
            mProgressDialog!!.dismiss()
        }
    }
}
