package com.burhanuday.potholego;

import android.content.Context;
import com.burhanuday.potholego.utils.Constants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by burhanuday on 18-11-2018.
 */

public class ApiClient {
    private static Retrofit retrofit = null;
    private static final int REQUEST_TIMEOUT = 60;
    private static OkHttpClient okHttpClient;

    /**
     * Return the instance of Retrofit
     * @param
     * @return
     */

    public static Retrofit getInstance(String token) {
        initOkHttp(token);
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    /**
     * initialise OkHttp Client
     * @param
     */

    private static void initOkHttp(final String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", token)
                            .build();
                    return chain.proceed(request);
                }
            });

        okHttpClient = httpClient.build();
    }
}
