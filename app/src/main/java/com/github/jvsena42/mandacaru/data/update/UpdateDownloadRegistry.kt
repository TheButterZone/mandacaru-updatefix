package com.github.jvsena42.mandacaru.data.update

import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource
import kotlinx.coroutines.runBlocking

/**
 * Persistent registry for APK downloads.
 *
 * Tracks the last downloaded version + download ID in PreferencesDataSource.
 * Acts as the single source of truth for update download state.
 */
class UpdateDownloadRegistry(
    private val context: Context,
    private val prefs: PreferencesDataSource
) {

    companion object {
        private const val KEY_LAST_VERSION = "update_last_version"
        private const val KEY_LAST_DOWNLOAD_ID = "update_last_download_id"
    }

    /**
     * Returns the active download ID, if any.
     */
    fun getActiveDownloadId(): Long? = runBlocking {
        val idStr = prefs.getString(PreferenceKeys(KEY_LAST_DOWNLOAD_ID), "-1")
        val downloadId = idStr.toLongOrNull()
        if (downloadId != null && downloadId != -1L) downloadId else null
    }

    /**
     * Returns the URI of the completed APK, if the last download succeeded.
     */
    fun getCompletedApkUri(): Uri? {
        val version = runBlocking { prefs.getString(PreferenceKeys(KEY_LAST_VERSION), "") }
        val downloadId = getActiveDownloadId() ?: return null

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val cursor = dm.query(android.app.DownloadManager.Query().setFilterById(downloadId)) ?: return null

        cursor.use {
            if (!it.moveToFirst()) return null
            val statusIndex = it.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS)
            val status = it.getInt(statusIndex)
            if (status != android.app.DownloadManager.STATUS_SUCCESSFUL) return null

            val uriIndex = it.getColumnIndex(android.app.DownloadManager.COLUMN_LOCAL_URI)
            val uriString = it.getString(uriIndex)
            return uriString?.let(Uri::parse)
        }
    }

    /**
     * Marks a version as currently downloading.
     */
    fun markDownloading(version: String, downloadId: Long) {
        runBlocking {
            prefs.setString(PreferenceKeys(KEY_LAST_VERSION), version)
            prefs.setString(PreferenceKeys(KEY_LAST_DOWNLOAD_ID), downloadId.toString())
        }
    }

    /**
     * Checks if a version is already downloaded.
     */
    fun isDownloaded(version: String): Boolean {
        val lastVersion = runBlocking { prefs.getString(PreferenceKeys(KEY_LAST_VERSION), "") }
        val uri = getCompletedApkUri()
        return lastVersion == version && uri != null
    }

    /**
     * Checks if a version is currently being downloaded.
     */
    fun isDownloading(version: String): Boolean {
        val lastVersion = runBlocking { prefs.getString(PreferenceKeys(KEY_LAST_VERSION), "") }
        val downloadId = getActiveDownloadId() ?: return false
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val cursor = dm.query(android.app.DownloadManager.Query().setFilterById(downloadId)) ?: return false

        cursor.use {
            if (!it.moveToFirst()) return false
            val statusIndex = it.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS)
            val status = it.getInt(statusIndex)
            return lastVersion == version && status == android.app.DownloadManager.STATUS_RUNNING
        }
    }
}
