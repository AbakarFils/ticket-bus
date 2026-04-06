package com.ticketbus.mobile.data.remote;

import com.ticketbus.mobile.util.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    public static ApiService create(String token) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging);

        if (token != null) {
            builder.addInterceptor(chain -> {
                Request req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
                return chain.proceed(req);
            });
        }

        return new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService.class);
    }
}
