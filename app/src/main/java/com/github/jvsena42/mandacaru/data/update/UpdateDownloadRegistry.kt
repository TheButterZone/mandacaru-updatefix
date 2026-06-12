package com.github.jvsena42.mandacaru.data.update

import com.github.jvsena42.mandacaru.data.PreferencesDataSource

/**
 * Prevents:
 * - duplicate downloads
 * - re-downloading same version
 * - file spam (-1, -2, etc.)
 * - inconsistent UI state after process death
 */
class UpdateDownloadRegistry(
    private val prefs: PreferencesDataSource
) {

    suspend fun isDownloading(version: String): Boolean {
        return prefs.getString(KEY_ACTIVE_VERSION, "") == version &&
                prefs.getBoolean(KEY_IS_DOWNLOADING, false)
    }

    suspend fun isDownloaded(version: String): Boolean {
        return prefs.getString(KEY_COMPLETED_VERSION, "") == version
    }

    suspend fun markDownloading(version: String, downloadId: Long) {
        prefs.setString(KEY_ACTIVE_VERSION, version)
        prefs.setString(KEY_DOWNLOAD_ID, downloadId.toString())
        prefs.setBoolean(KEY_IS_DOWNLOADING, true)

        // clear completed flag for safety (new attempt overrides old state)
        prefs.setBoolean(KEY_READY_TO_INSTALL, false)
    }

    suspend fun markCompleted(version: String, downloadId: Long) {
        prefs.setString(KEY_COMPLETED_VERSION, version)
        prefs.setBoolean(KEY_IS_DOWNLOADING, false)
        prefs.setString(KEY_DOWNLOAD_ID, downloadId.toString())

        prefs.setBoolean(KEY_READY_TO_INSTALL, true)
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
        const val KEY_READY_TO_INSTALL = "update_ready_to_install"
    }
}
