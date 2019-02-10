package com.burhanuday.potholego.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

/**
 * Created by burhanuday on 08-11-2018.
 */
class Pothole{
    var location:Location? = null
    var _id:String? = null
    var count:Int? = null
    var potholes:List<PotholeObject>? = null
    var isVerified:Boolean? = null
    var pitch:Float? = null
    var timestamp:String? = null

    class Location{
        var lat:Double? = null
        var lng:Double? = null
    }

    class PotholeObject{
        var length:Float? = null
        var width:Float? = null
        var height:Float? = null
    }
}