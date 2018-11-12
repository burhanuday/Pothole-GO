package com.burhanuday.potholego.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.burhanuday.potholego.R
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.*
import java.util.*

/**
 * Created by Burhanuddin on 27-10-2018.
 */

class CameraActivity : AppCompatActivity() {
    private val TAG: String = "AndroidCameraAPI"
    private lateinit var cameraID: String
    val ORIENTATIONS: SparseIntArray = SparseIntArray()
    val REQUEST_CAMERA_PERMISSION: Int = 200
    private var cameraDevice: CameraDevice? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var imageDimension: Size? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var captureRequest: CaptureRequest
    lateinit var cameraCaptureSession: CameraCaptureSession

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
            //Toast.makeText(baseContext, "Saved:" + file, Toast.LENGTH_SHORT).show()
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
            var captureBuilder: CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            //orientation
            val rotation: Int = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
            val file = File(Environment.getExternalStorageDirectory().toString() + "/pic.jpg")
            val readerListener: ImageReader.OnImageAvailableListener = object : ImageReader.OnImageAvailableListener{
                override fun onImageAvailable(reader: ImageReader?) {
                    var image: Image? = null
                    try {
                        image = reader!!.acquireLatestImage()
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        saves(bytes)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        image?.close()
                    }
                }

                fun saves(bytes: ByteArray){
                    var outputStream: OutputStream? = null
                    try {
                        outputStream = FileOutputStream(file)
                        outputStream.write(bytes)
                    }catch (e: IOException){

                    }finally {
                        outputStream?.close()
                    }
                }
            }

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback(){
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

            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback(){
                override fun onConfigureFailed(session: CameraCaptureSession?) {

                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    try {
                        session!!.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    }catch (e: CameraAccessException){
                        e.printStackTrace()
                    }
                }
            }, mBackgroundHandler)
        }catch (e: CameraAccessException){
            e.printStackTrace()
        }
    }

    protected fun createCameraPreview(){
        try {
            val texture: SurfaceTexture = textureView.surfaceTexture
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface: Surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback(){
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    Toast.makeText(baseContext, "Config change", Toast.LENGTH_SHORT).show()
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    if (cameraDevice == null){
                        return
                    }
                    cameraCaptureSession = session!!
                    updatePreview()
                }

            }, null)
        }catch (e: CameraAccessException){
            e.printStackTrace()
        }
    }

    private fun openCamera(){
        val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.i(TAG, "CAMERA IS OPEN")
        try {
            cameraID = manager.cameraIdList[0]
            val characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraID)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            //add permission for camera and ask user
            val perms = arrayOf("Manifest.permission.CAMERA", "Manifest.permission.WRITE_EXTERNAL_STORAGE")
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, perms, REQUEST_CAMERA_PERMISSION)
                return
            }
            manager.openCamera(cameraID, stateCallback, null)
        }catch (e: CameraAccessException){
            e.printStackTrace()
        }
        Log.i(TAG, "openCamera x")
    }

    protected fun updatePreview(){
        if (null == cameraDevice){
            Log.i(TAG, "Preview error")
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            try {
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler)
            }catch (e: CameraAccessException){
                e.printStackTrace()
            }
        }
    }

    private fun closeCamera(){
        if (null!=cameraDevice){
            cameraDevice?.close()
            cameraDevice=null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(baseContext, "ERROR OPENING CAM", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable){
            openCamera()
        }else{
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }


}
