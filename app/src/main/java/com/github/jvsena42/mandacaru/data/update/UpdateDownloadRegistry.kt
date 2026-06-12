package com.github.jvsena42.mandacaru.data.update

import com.github.jvsena42.mandacaru.data.PreferencesDataSource

/**
 * Single source of truth for update download state.
 *
 * Responsibilities:
 * - prevent duplicate downloads
 * - persist active download
 * - persist completed version
 * - allow recovery after app restart
 */
class UpdateDownloadRegistry(
    private val prefs: PreferencesDataSource
) {

    // ----------------------------
    // ACTIVE DOWNLOAD
    // ----------------------------

    suspend fun isDownloading(version: String): Boolean {
        return prefs.getString(KEY_ACTIVE_VERSION, "") == version
    }

    fun getActiveDownloadId(): Long? {
        return prefs.getString(KEY_DOWNLOAD_ID, null)?.toLongOrNull()
    }

    suspend fun getActiveVersion(): String? {
        return prefs.getString(KEY_ACTIVE_VERSION, "").takeIf { it.isNotEmpty() }
    }

    suspend fun markDownloading(version: String, downloadId: Long) {
        prefs.setString(KEY_ACTIVE_VERSION, version)
        prefs.setString(KEY_DOWNLOAD_ID, downloadId.toString())
        prefs.setBoolean(KEY_IS_DOWNLOADING, true)

        // clear completion state when a new download starts
        prefs.setString(KEY_COMPLETED_VERSION, "")
        prefs.setString(KEY_APK_URI, "")
    }

    // ----------------------------
    // COMPLETED DOWNLOAD
    // ----------------------------

    suspend fun isDownloaded(version: String): Boolean {
        return prefs.getString(KEY_COMPLETED_VERSION, "") == version
    }

    suspend fun markCompleted(version: String, uri: String) {
        prefs.setString(KEY_COMPLETED_VERSION, version)
        prefs.setString(KEY_APK_URI, uri)
        prefs.setBoolean(KEY_IS_DOWNLOADING, false)

        // keep active state consistent
        val current = prefs.getString(KEY_ACTIVE_VERSION, "")
        if (current == version) {
            prefs.setBoolean(KEY_IS_DOWNLOADING, false)
        }
    }

    fun getCompletedApkUri(): String? {
        return prefs.getString(KEY_APK_URI, null)
    }

    // ----------------------------
    // RECOVERY / CLEANUP
    // ----------------------------

    suspend fun clear(version: String) {
        val current = prefs.getString(KEY_ACTIVE_VERSION, "")
        if (current == version) {
            prefs.setString(KEY_ACTIVE_VERSION, "")
            prefs.setString(KEY_DOWNLOAD_ID, "")
            prefs.setBoolean(KEY_IS_DOWNLOADING, false)
        }
    }

    /**
     * This is what fixes your current resolver call.
     * It replaces ViewModel memory state entirely.
     */
    suspend fun getActiveDownloadState(): UpdateDownloadState? {
        val version = getActiveVersion() ?: return null
        val id = getActiveDownloadId() ?: return null

        return UpdateDownloadState(
            version = version,
            downloadId = id
        )
    }

    // ----------------------------
    // KEYS
    // ----------------------------

    private companion object {
        const val KEY_ACTIVE_VERSION = "update_active_version"
        const val KEY_COMPLETED_VERSION = "update_completed_version"
        const val KEY_DOWNLOAD_ID = "update_download_id"
        const val KEY_IS_DOWNLOADING = "update_is_downloading"
        const val KEY_APK_URI = "update_apk_uri"
    }
}
