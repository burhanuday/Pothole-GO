package com.burhanuday.potholego.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.burhanuday.potholego.R
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.burhanuday.potholego.utils.OpenCameraView
import com.burhanuday.potholego.utils.Constants
import com.burhanuday.potholego.utils.FolderUtil
import com.burhanuday.potholego.utils.Utilities
import kotlinx.android.synthetic.main.show_camera.*
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import java.io.File

/**
 * Created by burhanuday on 05-11-2018.
 */
class OpenCVCamera: AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    val TAG = "OPENCVCAMERA"
    private lateinit var mOpenCvCameraView: OpenCameraView
    private lateinit var mRgba:Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar!!.hide()
        setContentView(R.layout.show_camera)
        mOpenCvCameraView = findViewById<View>(R.id.show_camera_activity_java_surface_view) as OpenCameraView
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        mOpenCvCameraView.enableFpsMeter()


        /**
         * listens to changes when the flash button is pressed
         */
        switch_flash.setOnCheckedChangeListener { buttonView, isChecked ->
            mOpenCvCameraView.setFlash(isChecked)
        }

        /**
         * capture image when the capture button is pressed
         */
        fab_capture.setOnClickListener{
            val outPicture = Constants.SCAN_IMAGE_LOCATION + File.separator + Utilities.generateFilename()
            FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION)
            mOpenCvCameraView.takePicture(outPicture)
            Log.d(TAG, "Path $outPicture")
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(height, width, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame!!.rgba()
        return mRgba
    }

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    /**
     * Look for internet OPENCV library or else use OPENCV MANAGER
     */
    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }
}