package com.example.mapapp2.network;

import com.example.mapapp2.models.LoginRequest;
import com.example.mapapp2.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Map;

public interface ApiService {
    // Get user profile
    @GET("users/{id}")
    Call<Object> getUserProfile(@Path("id") int userId, @Header("Authorization") String token);

    // Get all images for a user
    @GET("images")
    Call<Object> getUserImages(@Header("Authorization") String token);


    // Login endpoint
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    //create new user account
    // Create new user account
    @POST("users")
    Call<Void> createUser(@Body Map<String, Object> payload);

}
