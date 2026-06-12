package com.github.jvsena42.mandacaru.data.update

import android.content.Context
import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource

class UpdateDownloadRegistry(
    private val preferences: PreferencesDataSource,
    private val context: Context
) {

    suspend fun isSameVersionAlreadyHandled(version: String): Boolean {
        val storedVersion = preferences.getString(KEY_VERSION, "")
        return storedVersion == version
    }

    suspend fun markVersion(version: String, downloadId: Long) {
        preferences.setString(KEY_VERSION, version)
        preferences.setString(KEY_DOWNLOAD_ID, downloadId.toString())
    }

    suspend fun getStoredDownloadId(): Long? {
        return preferences.getString(KEY_DOWNLOAD_ID, "")
            .toLongOrNull()
    }

    suspend fun clear() {
        preferences.setString(KEY_VERSION, "")
        preferences.setString(KEY_DOWNLOAD_ID, "")
    }

    companion object {
        private const val KEY_VERSION = "update_last_version"
        private const val KEY_DOWNLOAD_ID = "update_last_download_id"
    }
}
