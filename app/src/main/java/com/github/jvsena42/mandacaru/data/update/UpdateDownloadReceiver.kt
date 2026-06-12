package com.github.jvsena42.mandacaru.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Marks completed downloads so UI can resolve "Ready to Install"
 */
class UpdateDownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(downloadId)
        ) ?: return

        cursor.use {
            if (!it.moveToFirst()) return

            val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = it.getInt(statusIndex)

            if (status != DownloadManager.STATUS_SUCCESSFUL) return

            val uriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val uriString = it.getString(uriIndex)

            if (uriString.isNullOrEmpty()) return

            val uri = Uri.parse(uriString)

            // Persist minimal completion marker (optional future use)
            UpdateDownloadStateStoreLegacy.save(context, downloadId, uri)
        }
    }
}

/**
 * TEMP internal helper to avoid reintroducing a full registry system.
 * We keep persistence minimal and isolated.
 */
private object UpdateDownloadStateStoreLegacy {

    private const val PREFS = "update_state"
    private const val KEY_ID = "download_id"
    private const val KEY_URI = "apk_uri"

    fun save(context: Context, id: Long, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_ID, id)
            .putString(KEY_URI, uri.toString())
            .apply()
    }
}
