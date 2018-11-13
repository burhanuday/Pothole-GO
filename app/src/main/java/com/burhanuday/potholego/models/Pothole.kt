package com.burhanuday.potholego.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

/**
 * Created by burhanuday on 08-11-2018.
 */
class Pothole{

    var location:Location? = null
    var _id:String? = null
    var height:Double? = null
    var width:Double? = null
    var length:Double? = null
    var timestamp:String? = null
    var images:Images? = null


    class Location{
        var lat:Double? = null
        var lng:Double? = null
    }

    class Images{
        var original = arrayOf<String>()
        var processed = arrayOf<String>()
    }

    class ImagePhotos{
        var original = arrayOf<Bitmap>()
        var processed = arrayOf<Bitmap>()
    }
}