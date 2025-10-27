package com.example.final_project.network;



import com.example.final_project.models.dto.LogMealResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FoodApiService {
    @Multipart
    @POST("image/recognition/complete")
    Call<LogMealResponse> identifyFood(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );
}