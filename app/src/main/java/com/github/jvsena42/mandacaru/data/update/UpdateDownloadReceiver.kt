package com.github.jvsena42.mandacaru.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class UpdateDownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (downloadId == -1L) return

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query) ?: return

        cursor.use {
            if (!it.moveToFirst()) return

            val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = it.getInt(statusIndex)

            if (status != DownloadManager.STATUS_SUCCESSFUL) return

            val uriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val uriString = it.getString(uriIndex)

            if (uriString.isNullOrEmpty()) return

            val uri = Uri.parse(uriString)

            // Save completion marker for UI
            UpdateDownloadStateStore.saveCompleted(
                context = context,
                downloadId = downloadId,
                uri = uri
            )
        }
    }
}
