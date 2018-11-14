package com.burhanuday.potholego.utils;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;
import com.burhanuday.potholego.R;
import com.burhanuday.potholego.RESTApi;
import com.burhanuday.potholego.models.LocationHolder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.opencv.android.JavaCameraView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Burhanuddin on 4-11-2018.
 */

public class OpenCameraView extends JavaCameraView implements Camera.PictureCallback {

    private static final String TAG = OpenCameraView.class.getSimpleName();

    private String mPictureFileName;

    public static int minWidthQuality = 400;

    private Context context;
    Matrix matrix = new Matrix();
    private int focusAreaSize = getResources().getDimensionPixelSize(R.dimen.camera_focus_area_size);

    public boolean firstTaken = false;
    String first, second;

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
        Bitmap bm = rotate(bitmap, 90);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

        if (!firstTaken){
            first = mPictureFileName;
            //Toast.makeText(context, "first taken", Toast.LENGTH_SHORT).show();
            firstTaken = true;
        }else{
            second = mPictureFileName;
            //Toast.makeText(context, "second taken", Toast.LENGTH_SHORT).show();
            firstTaken = false;
            sendMultipleParts();
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

    private void sendMultipleParts(){
        List<MultipartBody.Part> parts = new ArrayList<>();
        parts.add(prepareFilePart("images", first));
        parts.add(prepareFilePart("images", second));
        RESTApi restApi = RESTApi.Companion.create();

        RequestBody lat = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getLATITUDE()));
        RequestBody lng = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getLONGITUDE()));
        Call<ResponseBody> req = restApi.postPothole(parts, lat, lng);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, String path){
        if (!FolderUtil.checkIfFileExist(path)){
            Toast.makeText(context, "ERROR GETTING FILE: DOES NOT EXIST", Toast.LENGTH_SHORT).show();
        }
        File file = new File(path);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestBody);
    }
}
