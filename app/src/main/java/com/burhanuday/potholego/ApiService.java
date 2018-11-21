package com.burhanuday.potholego;

import com.burhanuday.potholego.models.Pothole;
import com.burhanuday.potholego.models.User;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

/**
 * Created by burhanuday on 18-11-2018.
 */

public interface ApiService{

    @Multipart
    @POST("create")
    Call<ResponseBody> postPothole(@Part List<MultipartBody.Part> images,
                    @Part("lat") RequestBody latitude,
                    @Part("lng") RequestBody longitude);

    @GET("potholes")
    Call<List<Pothole>> getAll();

    @GET("potholes")
    Call<List<Pothole>> getNearbyPotholes(@Query("lat") Double latitude, @Query("lng") Double longitude);

    @POST("login")
    Call<User> authenticate(@Query("username") String username, @Query("email") String email);
}
