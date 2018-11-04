package com.burhanuday.potholego.Activities

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Toast
import com.burhanuday.potholego.R
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {

    private val TAG: String = "AndroidCameraAPI"
    private lateinit var cameraID: String
    val ORIENTATIONS: SparseIntArray = SparseIntArray()
    val REQUEST_CAMERA_PERMISSION: Int = 200
    private var cameraDevice: CameraDevice? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
        textureView.surfaceTextureListener = textureListener
        iv_capture.setOnClickListener{
            takePicture()
        }

    }

    private val textureListener = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }

    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private val captureCallbackListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback(){
        override fun onCaptureCompleted(
            session: CameraCaptureSession?,
            request: CaptureRequest?,
            result: TotalCaptureResult?
        ) {
            super.onCaptureCompleted(session, request, result)
            Toast.makeText(baseContext, "Saved:" + file, Toast.LENGTH_SHORT).show()
            createCameraPreview()
        }
    }

    protected fun startBackgroundThread(){
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    protected fun stopBackgroundThread(){
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        }catch (ie: InterruptedException){
            ie.printStackTrace()
        }
    }

    protected fun takePicture(){
        if (cameraDevice == null){
            return
        }

        val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics: CameraCharacteristics? = manager.getCameraCharacteristics(cameraDevice!!.id)
            var jpegSizes: Array<Size?>? = Array<Size?>(20){null}
            if (characteristics!= null){
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (jpegSizes!=null && 0 < jpegSizes.size){
                width = jpegSizes[0]!!.width
                height = jpegSizes[0]!!.height
            }

            val reader: ImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces: MutableList<Surface> = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView.surfaceTexture))
            val captureBuilder: CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

        }
    }


}
