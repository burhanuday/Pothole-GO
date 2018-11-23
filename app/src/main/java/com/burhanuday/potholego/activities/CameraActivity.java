package com.burhanuday.potholego.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import com.camerakit.CameraKitView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    private CameraKitView cameraKitView;
    private ProgressDialog mProgressDialog = null;
    public boolean firstTaken = false;
    String first, second;
    private Context context;
    private FloatingActionButton capture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraKitView = findViewById(R.id.camera);
        cameraKitView.setAspectRatio(1f);
        context = CameraActivity.this;

        capture = findViewById(R.id.fab_capture);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pictureOut = Constants.SCAN_IMAGE_LOCATION  + File.separator + Utilities.generateFilename();
                FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION);
                captureImage(pictureOut);
            }
        });

    }

    private void captureImage(final String mPictureFileName){
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] data) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Uri uri = Uri.parse(mPictureFileName);

                try {
                    FileOutputStream fos = new FileOutputStream(mPictureFileName);
                    //bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
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
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void sendMultipleParts(){
        showProgressDialog();
        List<MultipartBody.Part> parts = new ArrayList<>();
        parts.add(prepareFilePart("images", first));
        parts.add(prepareFilePart("images", second));
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.burhanuday.potholego", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        ApiService apiService = ApiClient.getInstance(token).create(ApiService.class);
        RequestBody lat = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getLATITUDE()));
        RequestBody lng = RequestBody.create(MediaType.parse("text/*"), Objects.requireNonNull(LocationHolder.INSTANCE.getLONGITUDE()));
        Call<ResponseBody> req = apiService.postPothole(parts, lat, lng);
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
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file);
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
}
