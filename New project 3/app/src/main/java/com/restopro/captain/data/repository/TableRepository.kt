package com.restopro.captain.data.repository

import com.restopro.captain.data.local.dao.TableDao
import com.restopro.captain.data.remote.api.TableApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepository @Inject constructor(
    private val tableDao: TableDao,
    private val tableApi: TableApi
) {
    fun observeTables() = tableDao.observeTables()

    suspend fun refreshTables() {
        tableDao.upsertTables(tableApi.tables().map { it.toEntity() })
    }
}
