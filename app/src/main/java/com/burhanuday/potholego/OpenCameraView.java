package com.burhanuday.potholego;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.features2d.Params;

import java.io.FileOutputStream;
import java.util.List;

public class OpenCameraView extends JavaCameraView implements Camera.PictureCallback {

    private static final String TAG = OpenCameraView.class.getSimpleName();

    private String mPictureFileName;

    public static int minWidthQuality = 400;

    private Context context;
    Matrix matrix = new Matrix();
    private int focusAreaSize = getResources().getDimensionPixelSize(R.dimen.camera_focus_area_size);


    public OpenCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setWillNotDraw(false);
    }

    //change value of flash in camera parameters
    public void setFlash(boolean value){
        if (value){
            mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }else {
            mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
    }

    //draw on top of camera preview
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(Color.parseColor("#000000"));
        //canvas.drawCircle(getWidth()/2, getHeight()/2, 20f, circlePaint);
        circleDraw.setStyle(Paint.Style.STROKE);
        circleDraw.setColor(getResources().getColor(R.color.colorPrimary));
        circleDraw.setStrokeWidth(12f);
        canvas.drawCircle(getWidth()/2, getHeight()/2, 300f, circleDraw);
    }

    protected final Paint circleDraw = new Paint();

    /*
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, getSurfaceView().getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, getSurfaceView().getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    protected void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {

            mCamera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
            Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //parameters.setFocusAreas(Lists.newArrayList(new Camera.Area(focusRect, 1000)));


           // parameters.setMeteringAreas(Lists.newArrayList(new Camera.Area(meteringRect, 1000)));

            mCamera.setParameters(parameters);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        //int left = clamp(Float.valueOf((x / getSurfaceView().getWidth()) * 2000 - 1000).intValue(), focusAreaSize);
        //int top = clamp(Float.valueOf((y / getSurfaceView().getHeight()) * 2000 - 1000).intValue(), focusAreaSize);

        //return new Rect(left, top, left + focusAreaSize, top + focusAreaSize);
    }
    */

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }


    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }


    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }


    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }


    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }



    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Uri uri = Uri.parse(mPictureFileName);

        Log.d(TAG, "selectedImage: " + uri);
        Bitmap bm = null;
        bm = rotate(bitmap, 90);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }


    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            return bmOut;
        }
        return bm;
    }
}
