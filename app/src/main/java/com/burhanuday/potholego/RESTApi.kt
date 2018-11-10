package com.burhanuday.potholego

import com.burhanuday.potholego.models.Pothole
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST

/**
 * Created by Burhanuddin on 27-10-2018.
 */

interface RESTApi {

    //https://medium.com/@adinugroho/upload-image-from-android-app-using-retrofit-2-ae6f922b184c
    @Multipart
    @POST
    fun postPothole():Call<Pothole>

    @GET
    fun getAll():Call<List<Pothole>>

    companion object {
        fun create(baseURL: String): RESTApi{
            val retrofit = Retrofit.Builder().baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(RESTApi::class.java)
        }
    }
}