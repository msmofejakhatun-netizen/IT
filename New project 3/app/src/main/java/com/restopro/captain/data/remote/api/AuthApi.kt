package com.restopro.captain.data.remote.api

import com.restopro.captain.data.remote.dto.HealthResponse
import com.restopro.captain.data.remote.dto.LoginRequest
import com.restopro.captain.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/captain/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/health")
    suspend fun health(): HealthResponse
}
