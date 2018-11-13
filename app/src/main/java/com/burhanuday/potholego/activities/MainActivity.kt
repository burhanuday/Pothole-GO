package com.burhanuday.potholego.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.burhanuday.potholego.R
import com.burhanuday.potholego.RESTApi
import com.burhanuday.potholego.models.Pothole
import com.burhanuday.potholego.utils.Constants
import com.burhanuday.potholego.utils.LocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

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
    var locationService:LocationService? = null
    var zoomable = false
    var zoomBlockingTimer: Timer? = null
    var handlerOnUiThread: Handler? = null
    var locationAccuracyCircle: Circle? = null
    var userPositionMarkerBitmapDescriptor: BitmapDescriptor? = null
    var userPositionMarker: Marker? = null
    var runningPathPolyline: Polyline? = null
    var polylineOptions: PolylineOptions? = null
    val polylineWidth: Int = 30
    var didInitialZoom: Boolean? = null
    var predictionRange: Circle? = null
    var oldLocationMarkerBitmapDescriptor: BitmapDescriptor? = null
    var noAccuracyLocationMarkerBitmapDescriptor: BitmapDescriptor? = null
    var inaccurateLocationMarkerBitmapDescriptor: BitmapDescriptor? = null
    var kalmanNGLocationMarkerBitmapDescriptor: BitmapDescriptor? = null
    var malMarkers: MutableList<Marker> = ArrayList()
    val handler: Handler = Handler()
    var locationUpdateReceiver: BroadcastReceiver? = null
    var predictedLocationReceiver: BroadcastReceiver? = null

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        googleMap ?: return
        with(googleMap) {
            moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(MUMBAI, ZOOM_LEVEL))
        }
        googleMap.isMyLocationEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        mapReady = true

        googleMap.setOnCameraMoveStartedListener(object : GoogleMap.OnCameraMoveStartedListener{
            override fun onCameraMoveStarted(reason: Int) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                    zoomable = false
                    if (zoomBlockingTimer!=null){
                        zoomBlockingTimer?.cancel()
                    }
                    handlerOnUiThread = Handler()
                    val task: TimerTask = object : TimerTask(){
                        override fun run() {
                            handlerOnUiThread!!.post(object : Runnable{
                                override fun run() {
                                    zoomBlockingTimer = null
                                    zoomable = true
                                }

                            })
                        }

                    }

                    zoomBlockingTimer = Timer()
                    zoomBlockingTimer!!.schedule(task, 10*1000)

                }
            }

        })
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

        locationUpdateReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val newLocation: Location = intent!!.getParcelableExtra("location")
                drawLocationAccuracyCircle(newLocation)
                drawUserPositionMarker(newLocation)

                zoomMapTo(newLocation)

                /* Filter Visualization */
                drawMalLocations()
            }
        }

         predictedLocationReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val predictedLocation: Location = intent!!.getParcelableExtra("location")
                drawPredictionRange(predictedLocation)
            }

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationUpdateReceiver!!, IntentFilter("LocationUpdated"))

        LocalBroadcastManager.getInstance(this).registerReceiver(
            predictedLocationReceiver!!, IntentFilter("PredictLocation"))

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

        val locationServiceIntent = Intent(this.applicationContext, LocationService::class.java)
        this.applicationContext.startService(locationServiceIntent)
        this.applicationContext.bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        oldLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.old_location_marker)
        noAccuracyLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.no_accuracy_location_marker)
        inaccurateLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.inaccurate_location_marker)
        kalmanNGLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.kalman_ng_location_marker)
    }

    val serviceConnection: ServiceConnection = object : ServiceConnection{
        override fun onServiceDisconnected(className: ComponentName?) {
            if (className!!.className.equals("LocationService")){
                locationService = null
            }
        }

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val name = className!!.className
            if (name.endsWith("LocationService")){
                locationService = (service as LocationService.LocationServiceBinder).getService()
                locationService?.startUpdatingLocation()
            }
        }

    }

    private fun checkRequiredPermissions(){
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION), Constants.PERMISSION_REQUEST_CODE)
        }
    }


    public override fun onDestroy() {
        try {
            if (locationUpdateReceiver != null) {
                unregisterReceiver(locationUpdateReceiver)
            }

            if (predictedLocationReceiver != null) {
                unregisterReceiver(predictedLocationReceiver)
            }
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }


        super.onDestroy()

    }

    private fun zoomMapTo(location: Location){
        val latLng = LatLng(location.latitude, location.longitude)
        if (didInitialZoom == false){
            try {
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f))
                didInitialZoom = true
                return
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        if (zoomable){
            try {
                zoomable = false
                googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng), object : GoogleMap.CancelableCallback{
                    override fun onCancel() {
                        zoomable = true
                    }

                    override fun onFinish() {
                        zoomable = true
                    }
                })
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    private fun drawUserPositionMarker(location: Location){
        val latLng = LatLng(location.latitude, location.longitude)
        if (userPositionMarkerBitmapDescriptor == null){
            userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.user_position_point)
        }

        if (userPositionMarker == null){
            userPositionMarker = googleMap!!.addMarker(MarkerOptions()
                .position(latLng)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(userPositionMarkerBitmapDescriptor))
        }else{
            userPositionMarker!!.position = latLng
        }
    }

    private fun drawLocationAccuracyCircle(location: Location){
        if (location.accuracy<0){
            return
        }
        val latLng = LatLng(location.latitude, location.longitude)
        if (locationAccuracyCircle == null){
            locationAccuracyCircle = googleMap!!.addCircle(CircleOptions()
                .center(latLng)
                .fillColor(Color.argb(64,0,0,0))
                .strokeColor(Color.argb(64,0,0,0))
                .strokeWidth(0.0f)
                .radius(location.accuracy.toDouble()))
        }else{
            locationAccuracyCircle!!.center = latLng
        }
    }

    private fun drawMalLocations() {
        drawMalMarkers(locationService!!.oldLocationList, oldLocationMarkerBitmapDescriptor)
        drawMalMarkers(locationService!!.noAccuracyLocationList, noAccuracyLocationMarkerBitmapDescriptor)
        drawMalMarkers(locationService!!.inaccurateLocationList, inaccurateLocationMarkerBitmapDescriptor)
        drawMalMarkers(locationService!!.kalmanNGLocationList, kalmanNGLocationMarkerBitmapDescriptor)
    }

    private fun drawMalMarkers(locationList: ArrayList<Location>, descriptor: BitmapDescriptor?){
        for (location in locationList){
            val latLng = LatLng(location.latitude, location.longitude)
            val marker:Marker = googleMap!!.addMarker(MarkerOptions()
                .position(latLng)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(descriptor))

            malMarkers.add(marker)
        }
    }

    private fun drawPredictionRange(location: Location){
        val latLng = LatLng(location.latitude, location.longitude)
        if (predictionRange == null){
            predictionRange = googleMap!!.addCircle(CircleOptions()
                .center(latLng)
                .fillColor(Color.argb(50,30,207,0))
                .strokeColor(Color.argb(128,30,207,0))
                .strokeWidth(1.0f)
                .radius(30.0))
        }else{
            predictionRange!!.center = latLng
        }

        predictionRange!!.isVisible = true
        handler.postDelayed(object : Runnable{
            override fun run() {
                predictionRange!!.isVisible = false
            }

        }, 2000)
    }

    fun clearMalMarkers(){
        for (marker in malMarkers){
            marker.remove()
        }
    }


}
