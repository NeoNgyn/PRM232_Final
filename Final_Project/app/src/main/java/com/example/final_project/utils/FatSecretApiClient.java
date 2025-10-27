package com.example.final_project.utils;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory; // ✅ Thêm import này

public class FatSecretApiClient {
    private static final String BASE_URL = "https://platform.fatsecret.com/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create()) // ✅ đúng converter cho ResponseBody
                    .build();
        }
        return retrofit;
    }
}
