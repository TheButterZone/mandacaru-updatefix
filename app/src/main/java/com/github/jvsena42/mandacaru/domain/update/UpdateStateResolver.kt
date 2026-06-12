package com.github.jvsena42.mandacaru.domain.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.data.update.UpdateDownloadState

class UpdateStateResolver(
    private val context: Context
) {

    private val dm =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun resolve(
        status: com.github.jvsena42.mandacaru.domain.model.UpdateStatus,
        download: UpdateDownloadState?
    ): UpdateState {

        // No update available
        if (!status.isUpdateAvailable) {
            return UpdateState.Idle
        }

        // No download started yet → still idle
        if (download == null) {
            return UpdateState.Idle
        }

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(download.downloadId)
        ) ?: return UpdateState.Idle

        cursor.use {
            if (!it.moveToFirst()) return UpdateState.Idle

            val statusIndex =
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)

            return when (it.getInt(statusIndex)) {

                DownloadManager.STATUS_RUNNING ->
                    UpdateState.Downloading

                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uriIndex =
                        it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)

                    val uriString = it.getString(uriIndex)
                    val uri = uriString?.let(Uri::parse)

                    if (uri != null) {
                        UpdateState.ReadyToInstall(uri)
                    } else {
                        UpdateState.Idle
                    }
                }

                else -> UpdateState.Idle
            }
        }
    }
}
