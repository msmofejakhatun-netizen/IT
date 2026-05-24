package com.restopro.captain.data.remote.api

import com.restopro.captain.data.remote.dto.TableDto
import retrofit2.http.GET

interface TableApi {
    @GET("api/captain/tables")
    suspend fun tables(): List<TableDto>
}
