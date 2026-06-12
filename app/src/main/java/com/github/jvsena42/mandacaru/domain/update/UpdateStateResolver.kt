package com.github.jvsena42.mandacaru.domain.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.data.update.UpdateDownloadState

/**
 * Converts raw DownloadManager + app state into UI-friendly update states.
 *
 * THIS is what drives:
 * - "Download" button
 * - "Downloading" progress state
 * - "Install" button visibility
 */
class UpdateStateResolver(
    private val context: Context
) {

    private val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun resolve(
        status: com.github.jvsena42.mandacaru.domain.model.UpdateStatus,
        download: UpdateDownloadState?,
        downloadUri: Uri?
    ): UpdateState {

        // 1. No update available at all
        if (!status.isUpdateAvailable) {
            return UpdateState.NoUpdate
        }

        // 2. Already downloaded + marked ready
        if (download?.isCompleted == true || downloadUri != null) {
            return UpdateState.ReadyToInstall(downloadUri ?: Uri.EMPTY)
        }

        // 3. Active download
        if (download != null) {

            val cursor = dm.query(
                DownloadManager.Query().setFilterById(download.downloadId)
            ) ?: return UpdateState.Available

            cursor.use {
                if (!it.moveToFirst()) return UpdateState.Available

                val statusIndex = it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                val dmStatus = it.getInt(statusIndex)

                return when (dmStatus) {

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

        // 4. Default fallback
        return UpdateState.Available
    }
}
