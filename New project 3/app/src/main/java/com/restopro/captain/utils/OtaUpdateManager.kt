package com.restopro.captain.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtaUpdateManager @Inject constructor() {
    data class UpdateInfo(
        val latestVersion: String,
        val minSupportedVersion: String,
        val changelog: List<String>,
        val apkUrl: String,
        val forceUpdate: Boolean
    )

    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? {
        // Hook backend endpoint here and return null when app is current.
        return null
    }
}
