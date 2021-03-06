package com.burhanuday.potholego.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.burhanuday.potholego.ApiClient;
import com.burhanuday.potholego.ApiService;
import com.burhanuday.potholego.R;
import com.burhanuday.potholego.models.LocationHolder;
import com.burhanuday.potholego.utils.Constants;
import com.burhanuday.potholego.utils.FolderUtil;
import com.burhanuday.potholego.utils.Utilities;
import com.otaliastudios.cameraview.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog = null;
    private boolean isFlashOn = false;
    String first;
    private Context context;
    private FloatingActionButton capture, flash;
    private CameraView cameraView;
    private boolean updateMode = false;
    private double lat = 0;
    private double lng = 0;
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        if (getIntent().hasExtra("_id") && getIntent().hasExtra("lng") && getIntent().hasExtra("lat")){
            updateMode = true;
            id = getIntent().getStringExtra("_id");
            lat = getIntent().getDoubleExtra("lat", -1);
            lng = getIntent().getDoubleExtra("lng", -1);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        context = CameraActivity.this;

        capture = findViewById(R.id.fab_capture);
        flash = findViewById(R.id.fab_flash);

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashOn){
                    cameraView.setFlash(Flash.OFF);
                    flash.setImageResource(R.drawable.ic_flash_off);
                    isFlashOn = false;
                }else {
                    cameraView.setFlash(Flash.ON);
                    flash.setImageResource(R.drawable.ic_flash_on);
                    isFlashOn = true;
                }

            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.capturePicture();
            }
        });

        cameraView = findViewById(R.id.camera);
        cameraView.setFlash(Flash.OFF);
        cameraView.setLifecycleOwner(this);
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        cameraView.mapGesture(Gesture.SCROLL_VERTICAL, GestureAction.EXPOSURE_CORRECTION);
        cameraView.setJpegQuality(100);
        cameraView.setCropOutput(true);

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                String pictureOut = Constants.SCAN_IMAGE_LOCATION  + File.separator + Utilities.generateFilename();
                FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION);
                captureImage(pictureOut, jpeg);
                super.onPictureTaken(jpeg);
            }
        });

    }

    private void captureImage(final String mPictureFileName, byte[] data){
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
        first = mPictureFileName;
        if(updateMode){
            sendUpdate();
        }else{
            sendMultipleParts();
        }
    }

    private void sendUpdate(){
        showProgressDialog();
        MultipartBody.Part parts = prepareFilePart("updatedimage", first);
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.burhanuday.potholego", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        ApiService apiService = ApiClient.getInstance(token).create(ApiService.class);
        RequestBody lat = RequestBody.create(MediaType.parse("text/*"), String.valueOf(this.lat));
        RequestBody lng = RequestBody.create(MediaType.parse("text/*"), String.valueOf(this.lng));
        Call<ResponseBody> req = apiService.updatePothole(parts, lat, lng, this.id);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                finish();
            }
        });
    }

    private void sendMultipleParts(){
        showProgressDialog();
        MultipartBody.Part parts = prepareFilePart("image", first);
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.burhanuday.potholego", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        ApiService apiService = ApiClient.getInstance(token).create(ApiService.class);
        RequestBody lat = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getLATITUDE()));
        RequestBody lng = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getLONGITUDE()));
        RequestBody pitch = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getPITCH()));
        Call<ResponseBody> req = apiService.postPothole(parts, lat, lng, pitch);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                finish();
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, String path){
        if (!FolderUtil.checkIfFileExist(path)){
            Toast.makeText(context, "ERROR GETTING FILE: DOES NOT EXIST", Toast.LENGTH_SHORT).show();
        }
        File file = new File(path);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestBody);
    }

    private void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = ProgressDialog.show(context, "Uploading", "Sending data...", true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog(){
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}
