package com.example.mapapp2;

import retrofit2.Call;
import retrofit2.http.GET;
public interface ApiService {
//    @GET("api/some-endpoint") // Replace with your actual endpoint
//    Call<MyResponseObject> getSomeData();

    @GET("ping")
    Call<PingResponse> pingServer();

}
