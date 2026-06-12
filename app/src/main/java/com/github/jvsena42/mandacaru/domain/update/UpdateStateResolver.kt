package com.github.jvsena42.mandacaru.domain.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri

/**
 * Converts DownloadManager state into UI state.
 */
class UpdateStateResolver(
    context: Context
) {

    private val dm =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun resolve(
        status: com.github.jvsena42.mandacaru.domain.model.UpdateStatus,
        downloadId: Long?
    ): UpdateState {

        if (!status.isUpdateAvailable) {
            return UpdateState.NoUpdate
        }

        if (downloadId == null) {
            return UpdateState.Available
        }

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(downloadId)
        ) ?: return UpdateState.Available

        cursor.use {
            if (!it.moveToFirst()) return UpdateState.Available

            val statusIndex = it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
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
                        UpdateState.Available
                    }
                }

                else -> UpdateState.Available
            }
        }
    }
}
