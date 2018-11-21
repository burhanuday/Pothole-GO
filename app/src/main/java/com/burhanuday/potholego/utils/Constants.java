package com.burhanuday.potholego.utils;

import android.os.Environment;

import java.io.File;

public class Constants {


    private Constants(){
        //cannot be instantiated
    }

    //URI of default folder where images are saved
    public static final String SCAN_IMAGE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "OpenScanner";
    public static final int PERMISSION_REQUEST_CODE = 123;
    public static final String BASE_URL = "https://fierce-thicket-79271.herokuapp.com/api/v1/";
}
