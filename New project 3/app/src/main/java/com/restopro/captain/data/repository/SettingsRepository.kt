package com.restopro.captain.data.repository

import com.restopro.captain.data.local.dao.SettingsDao
import com.restopro.captain.data.local.entity.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    fun observeSettings() = settingsDao.observeSettings()

    suspend fun savePrinter(address: String) {
        val settings = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.saveSettings(settings.copy(printerAddress = address))
    }
}
