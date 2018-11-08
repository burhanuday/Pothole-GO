package com.burhanuday.potholego.utils;

import android.os.Environment;

import java.io.File;

public class Constants {


    private Constants(){
        //cannot be instantiated
    }

    //URI of default folder where images are saved
    public static final String SCAN_IMAGE_LOCATION = Environment.getExternalStorageDirectory() + File.separator + "OpenScanner";
}
