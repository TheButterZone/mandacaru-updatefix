package com.github.jvsena42.mandacaru.data.update

import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource

object UpdateDownloadStateStore {

    suspend fun saveCompleted(
        context: Context,
        downloadId: Long,
        uri: Uri
    ) {
        val prefs = PreferencesDataSource(context)

        prefs.setString(KEY_COMPLETED_ID, downloadId.toString())
        prefs.setString(KEY_APK_URI, uri.toString())
        prefs.setBoolean(KEY_READY_TO_INSTALL, true)
    }

    suspend fun isReadyToInstall(context: Context): Boolean {
        val prefs = PreferencesDataSource(context)
        return prefs.getBoolean(KEY_READY_TO_INSTALL, false)
    }

    suspend fun getApkUri(context: Context): Uri? {
        val prefs = PreferencesDataSource(context)
        val uri = prefs.getString(KEY_APK_URI, "")
        return uri.takeIf { it.isNotEmpty() }?.let(Uri::parse)
    }

    suspend fun clear(context: Context) {
        val prefs = PreferencesDataSource(context)
        prefs.setBoolean(KEY_READY_TO_INSTALL, false)
        prefs.setString(KEY_APK_URI, "")
        prefs.setString(KEY_COMPLETED_ID, "")
    }

    private const val KEY_READY_TO_INSTALL = "update_ready_to_install"
    private const val KEY_APK_URI = "update_apk_uri"
    private const val KEY_COMPLETED_ID = "update_completed_id"
}
