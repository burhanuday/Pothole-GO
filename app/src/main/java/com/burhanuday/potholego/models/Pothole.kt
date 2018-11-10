package com.burhanuday.potholego.models

import com.google.gson.annotations.SerializedName

/**
 * Created by burhanuday on 08-11-2018.
 */
class Pothole() {

    constructor(latitude:Double, longitude:Double, height: Double, length:Double,
                width:Double, timestamp:String):this(){

    }

    class Location{
        var latitude:Double? = null
        var longitude:Double? = null
    }

    class Images{

    }
}