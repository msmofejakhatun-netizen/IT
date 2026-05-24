package com.restopro.captain.data.remote.api

import com.restopro.captain.data.remote.dto.OrderPayload
import com.restopro.captain.data.remote.dto.SyncResult
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApi {
    @POST("api/captain/orders")
    suspend fun upsertOrder(@Body payload: OrderPayload): SyncResult

    @POST("api/captain/orders/cancel")
    suspend fun cancelOrder(@Body payload: Map<String, String>): SyncResult
}
