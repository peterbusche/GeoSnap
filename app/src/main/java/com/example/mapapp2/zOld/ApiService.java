package com.example.mapapp2.zOld;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
public interface ApiService {
//    @GET("api/some-endpoint") // Replace with your actual endpoint
//    Call<MyResponseObject> getSomeData();

    @GET("ping")
    Call<PingResponse> pingServer();

    @FormUrlEncoded
    @POST("/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

}
