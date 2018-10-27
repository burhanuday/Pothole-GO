package com.burhanuday.potholego

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Burhanuddin on 27-10-2018.
 */

interface RESTApi {

    companion object {
        fun create(baseURL: String): RESTApi{
            val retrofit = Retrofit.Builder().baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(RESTApi::class.java)
        }
    }
}