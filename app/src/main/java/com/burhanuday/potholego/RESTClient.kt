package com.burhanuday.potholego

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by burhanuday on 19-11-2018.
 */

public class RESTClient{
    private var retrofit: Retrofit? = null
    private val REQUEST_TIMEOUT: Long = 60
    private var okHttpClient: OkHttpClient? = null

    fun getClient(context: Context): Retrofit{
        if (okHttpClient == null){
            initOkHttp(context)
        }

        if (retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl("https://fierce-thicket-79271.herokuapp.com/api/v1/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!
    }

    fun initOkHttp(context: Context){
        val httpClient: OkHttpClient.Builder = OkHttpClient().newBuilder()
            .connectTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)

        okHttpClient = httpClient.build()
    }
}