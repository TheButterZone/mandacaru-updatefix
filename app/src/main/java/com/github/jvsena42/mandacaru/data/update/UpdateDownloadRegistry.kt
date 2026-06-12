package com.github.jvsena42.mandacaru.data.update

import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource

/**
 * Minimal persistent registry:
 * - prevents duplicate downloads
 * - stores last active download id + version
 *
 * DOES NOT track install state.
 * DOES NOT query DownloadManager.
 */
class UpdateDownloadRegistry(
    private val prefs: PreferencesDataSource
) {

    suspend fun isDownloading(version: String): Boolean {
        val activeVersion = prefs.getString(KEY_ACTIVE_VERSION, "")
        val isDownloading = prefs.getBoolean(KEY_IS_DOWNLOADING, false)
        return activeVersion == version && isDownloading
    }

    suspend fun isDownloaded(version: String): Boolean {
        val completedVersion = prefs.getString(KEY_COMPLETED_VERSION, "")
        return completedVersion == version
    }

    suspend fun markDownloading(version: String, downloadId: Long) {
        prefs.setString(KEY_ACTIVE_VERSION, version)
        prefs.setString(KEY_DOWNLOAD_ID, downloadId.toString())
        prefs.setBoolean(KEY_IS_DOWNLOADING, true)
    }

    suspend fun markCompleted(version: String) {
        prefs.setString(KEY_COMPLETED_VERSION, version)
        prefs.setBoolean(KEY_IS_DOWNLOADING, false)
    }

    fun getActiveDownloadId(): Long? {
        return prefs.getString(KEY_DOWNLOAD_ID, "").toLongOrNull()
    }

    suspend fun clearIfMatches(version: String) {
        val active = prefs.getString(KEY_ACTIVE_VERSION, "")
        if (active == version) {
            prefs.setString(KEY_ACTIVE_VERSION, "")
            prefs.setString(KEY_DOWNLOAD_ID, "")
            prefs.setBoolean(KEY_IS_DOWNLOADING, false)
        }
    }

    private companion object {
        const val KEY_ACTIVE_VERSION = "update_active_version"
        const val KEY_COMPLETED_VERSION = "update_completed_version"
        const val KEY_DOWNLOAD_ID = "update_download_id"
        const val KEY_IS_DOWNLOADING = "update_is_downloading"
    }
}
