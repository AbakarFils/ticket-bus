package com.ticketbus.mobile.data.remote;

import com.ticketbus.mobile.data.remote.dto.AuthResponse;
import com.ticketbus.mobile.data.remote.dto.BlacklistDto;
import com.ticketbus.mobile.data.remote.dto.LoginRequest;
import com.ticketbus.mobile.data.remote.dto.PublicKeyDto;
import com.ticketbus.mobile.data.remote.dto.SyncResponse;
import com.ticketbus.mobile.data.remote.dto.SyncUploadRequest;
import com.ticketbus.mobile.data.remote.dto.ValidationRequest;
import com.ticketbus.mobile.data.remote.dto.ValidationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest req);

    @POST("validation/validate")
    Call<ValidationResponse> validate(@Body ValidationRequest req);

    @POST("sync/upload")
    Call<SyncResponse> uploadSync(@Body SyncUploadRequest req);

    @GET("sync/blacklist")
    Call<List<BlacklistDto>> getBlacklist(@Query("since") String since);

    @GET("sync/public-key")
    Call<PublicKeyDto> getPublicKey();
}
