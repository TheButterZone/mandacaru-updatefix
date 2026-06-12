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

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(downloadId)
        ) ?: return

        cursor.use {
            if (!it.moveToFirst()) return

            val status = it.getInt(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )

            if (status != DownloadManager.STATUS_SUCCESSFUL) return

            val uriString = it.getString(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            )

            val uri = Uri.parse(uriString)

            /**
             * Mark as completed in persistent store
             */
            UpdateDownloadStateStore.saveCompleted(
                context = context,
                downloadId = downloadId,
                uri = uri
            )

            /**
             * IMPORTANT: prevent future duplicate downloads
             * (this closes the loop with UpdateDownloadRegistry)
             */
            val prefs = context.getSharedPreferences("updates", Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("downloadId", downloadId)
                .apply()
        }
    }
}
