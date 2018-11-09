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
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import com.burhanuday.potholego.OpenCameraView
import com.burhanuday.potholego.utils.Constants
import com.burhanuday.potholego.utils.FolderUtil
import com.burhanuday.potholego.utils.Utilities
import kotlinx.android.synthetic.main.show_camera.*
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File

/**
 * Created by burhanuday on 05-11-2018.
 */
class OpenCVCamera: AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    val TAG = "OPENCVCAMERA"
    private lateinit var mOpenCvCameraView: OpenCameraView
    private var isJavaCamera: Boolean = true
    private lateinit var mItemSwitchCamera: MenuItem
    private lateinit var mRgba:Mat
    private lateinit var mRgbaF:Mat
    private lateinit var mRgbaT:Mat
    private lateinit var imgGray:Mat
    private lateinit var imgCanny:Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar!!.hide()
        setContentView(R.layout.show_camera)
        mOpenCvCameraView = findViewById<View>(R.id.show_camera_activity_java_surface_view) as OpenCameraView
        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        //mOpenCvCameraView.disableFpsMeter()
        //mOpenCvCameraView.setMaxFrameSize(1920, 1440)
        //val sizes: MutableList<Camera.Size>? = mOpenCvCameraView.resolutionList
        //mOpenCvCameraView.resolution = sizes!![0]
        //mOpenCvCameraView.setMaxFrameSize()

        //listens to changes when the flash switch is used
        switch_flash.setOnCheckedChangeListener { buttonView, isChecked ->
            mOpenCvCameraView.setFlash(isChecked)
        }

        //capture button listener
        iv_capture.setOnClickListener{
            val outPicture = Constants.SCAN_IMAGE_LOCATION + File.separator + Utilities.generateFilename()
            FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION)
            mOpenCvCameraView.takePicture(outPicture)
            Toast.makeText(this@OpenCVCamera, "Picture has been taken ", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Path $outPicture")
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(height, width, CvType.CV_8UC4)
        mRgbaF = Mat(height, width, CvType.CV_8UC4)
        mRgbaT = Mat(width, width, CvType.CV_8UC4)
        imgGray = Mat(height, width, CvType.CV_8UC1)
        imgCanny = Mat(height, width, CvType.CV_8UC1)
    }

    override fun onCameraViewStopped() {
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame!!.rgba()
        //preprocessor.changeImagePreviewOrientation(mRgba, des, forward)
        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGBA2GRAY)
        val s = Size(5.0, 5.0)
        Imgproc.GaussianBlur(imgGray, imgGray, s, 0.0)
        Imgproc.medianBlur(imgGray, imgGray, 5)
        val mat: Mat = Mat.ones(Size(5.0,5.0), 5)
        Imgproc.erode(imgGray, imgGray, mat)
        Imgproc.dilate(imgGray, imgGray, mat)
        Imgproc.morphologyEx(imgGray, imgGray, Imgproc.MORPH_CLOSE, mat)
        Imgproc.Canny(imgGray, imgCanny, 9.0, 220.0)
        //return mRgba //This function must return
        return imgCanny
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