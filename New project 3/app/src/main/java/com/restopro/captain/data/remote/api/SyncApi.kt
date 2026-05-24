package com.restopro.captain.data.remote.api

import com.restopro.captain.data.remote.dto.SyncEnvelope
import com.restopro.captain.data.remote.dto.SyncResult
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApi {
    @POST("api/captain/sync")
    suspend fun sync(@Body envelope: SyncEnvelope): SyncResult
}
