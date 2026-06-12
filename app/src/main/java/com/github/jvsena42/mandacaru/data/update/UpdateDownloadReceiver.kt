package com.github.jvsena42.mandacaru.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Receives DownloadManager completion events and
 * updates persistent update state so UI can show:
 * - "Install" button
 * - "Download completed" state
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

            if (status != DownloadManager.STATUS_SUCCESSFUL) {
                Log.w("UpdateReceiver", "Download failed or incomplete: $downloadId")
                return
            }

            val uriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val uriString = it.getString(uriIndex)

            if (uriString.isNullOrEmpty()) return

            val uri = Uri.parse(uriString)

            // Recover version from registry context (prevents "unknown version" bugs)
            val prefs = PreferencesDataSource(context)

            val version = prefs.getString(
                "update_active_version",
                "unknown"
            )

            // Mark registry state as completed + install-ready
            val registry = UpdateDownloadRegistry(prefs)

            registry.markCompleted(
                version = version,
                downloadId = downloadId
            )

            Log.d("UpdateReceiver", "Update ready to install: $version ($uri)")
        }
    }
}
