package com.restopro.captain.data.remote.api

import com.restopro.captain.data.remote.dto.KotRequest
import com.restopro.captain.data.remote.dto.SyncResult
import retrofit2.http.Body
import retrofit2.http.POST

interface KotApi {
    @POST("api/captain/kot/send")
    suspend fun sendKot(@Body request: KotRequest): SyncResult
}
