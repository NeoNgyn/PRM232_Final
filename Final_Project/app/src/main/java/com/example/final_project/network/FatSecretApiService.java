package com.example.final_project.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface FatSecretApiService {

    // Endpoint chính xác của FatSecret API v2/v5
    @GET("rest/server.api")
    Call<ResponseBody> searchFoods(
            @Header("Authorization") String token,
            @Query("method") String method,
            @Query("search_expression") String query,
            @Query("format") String format
    );

    @GET("rest/server.api")
    Call<ResponseBody> getFoodById(
            @Header("Authorization") String token,
            @Query("method") String method,
            @Query("food_id") String foodId,
            @Query("format") String format
    );
}
