package com.burhanuday.potholego.utils;

import android.util.Log;

import java.io.File;

public class FolderUtil {
    private static final String TAG = "FolderUtil";

    private FolderUtil(){
        //class cannot be instantiated
    }

    public static void createDefaultFolder(String dirPath){
        File directory = new File(dirPath);
        if(!directory.exists()){
           directory.mkdir();
        }
    }


    public static boolean checkIfFileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }
}
