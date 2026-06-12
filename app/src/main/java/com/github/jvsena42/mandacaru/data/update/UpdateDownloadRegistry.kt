package com.github.jvsena42.mandacaru.data.update

import android.content.Context
import com.github.jvsena42.mandacaru.data.PreferencesDataSource

/**
 * Prevents:
 * - duplicate downloads
 * - re-downloading same version
 * - file spam (-1, -2, etc.)
 */
class UpdateDownloadRegistry(
    private val prefs: PreferencesDataSource
) {

    suspend fun isDownloading(version: String): Boolean {
        return prefs.getString(KEY_ACTIVE_VERSION, "") == version
    }

    suspend fun isDownloaded(version: String): Boolean {
        return prefs.getString(KEY_COMPLETED_VERSION, "") == version
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

    suspend fun clear(version: String) {
        val current = prefs.getString(KEY_ACTIVE_VERSION, "")
        if (current == version) {
            prefs.setString(KEY_ACTIVE_VERSION, "")
            prefs.setString(KEY_DOWNLOAD_ID, "")
            prefs.setBoolean(KEY_IS_DOWNLOADING, false)
        }
    }

    fun getActiveDownloadId(): Long? {
        return prefs.getString(KEY_DOWNLOAD_ID, null)?.toLongOrNull()
    }

    private companion object {
        const val KEY_ACTIVE_VERSION = "update_active_version"
        const val KEY_COMPLETED_VERSION = "update_completed_version"
        const val KEY_DOWNLOAD_ID = "update_download_id"
        const val KEY_IS_DOWNLOADING = "update_is_downloading"
    }
}
