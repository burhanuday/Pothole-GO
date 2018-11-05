package com.burhanuday.potholego.Activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.burhanuday.potholego.R
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import android.view.SurfaceView
import android.view.View
import org.opencv.android.JavaCameraView
import android.view.WindowManager
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc


/**
 * Created by burhanuday on 05-11-2018.
 */
class OpenCVCamera: AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    val TAG = "OPENCVCAMERA"
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private var isJavaCamera: Boolean = true
    private lateinit var mItemSwitchCamera: MenuItem
    lateinit var mRgba:Mat
    lateinit var mRgbaF:Mat
    lateinit var mRgbaT:Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar!!.hide()

        setContentView(R.layout.show_camera)

        mOpenCvCameraView = findViewById<View>(R.id.show_camera_activity_java_surface_view) as JavaCameraView

        mOpenCvCameraView.visibility = SurfaceView.VISIBLE

        mOpenCvCameraView.setCvCameraViewListener(this)
    }
    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(height, width, CvType.CV_8UC4)
        mRgbaF = Mat(height, width, CvType.CV_8UC4)
        mRgbaT = Mat(width, width, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame!!.rgba()
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT)
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0.0,0.0, 0)
        Core.flip(mRgbaF, mRgba, 1 )
        return mRgba // This function must return
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
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView()
    }

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
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView()
    }
}