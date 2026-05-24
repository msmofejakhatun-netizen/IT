package com.restopro.captain.data.remote.api

import com.restopro.captain.data.remote.dto.CategoryDto
import com.restopro.captain.data.remote.dto.MenuItemDto
import retrofit2.http.GET

interface MenuApi {
    @GET("api/captain/menu/categories")
    suspend fun categories(): List<CategoryDto>

    @GET("api/captain/menu/items")
    suspend fun items(): List<MenuItemDto>
}
