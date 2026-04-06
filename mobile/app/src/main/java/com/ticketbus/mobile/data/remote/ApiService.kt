package com.ticketbus.mobile.data.remote

import com.ticketbus.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>
    @POST("validation/validate")
    suspend fun validate(@Body req: ValidationRequest): Response<ValidationResponse>
    @POST("sync/upload")
    suspend fun uploadSync(@Body req: SyncUploadRequest): Response<SyncResponse>
    @GET("sync/blacklist")
    suspend fun getBlacklist(@Query("since") since: String?): Response<List<BlacklistDto>>
    @GET("sync/public-key")
    suspend fun getPublicKey(): Response<PublicKeyDto>
}
