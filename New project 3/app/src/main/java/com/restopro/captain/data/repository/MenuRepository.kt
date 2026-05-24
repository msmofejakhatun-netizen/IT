package com.restopro.captain.data.repository

import com.restopro.captain.data.local.dao.MenuDao
import com.restopro.captain.data.remote.api.MenuApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val menuDao: MenuDao,
    private val menuApi: MenuApi
) {
    fun observeCategories() = menuDao.observeCategories()
    fun observeItems(categoryId: String?, query: String) = menuDao.observeMenuItems(categoryId, query)

    suspend fun refreshMenu() {
        val categories = menuApi.categories().map { it.toEntity() }
        val items = menuApi.items().map { it.toEntity() }
        menuDao.upsertCategories(categories)
        menuDao.upsertMenuItems(items)
    }
}
