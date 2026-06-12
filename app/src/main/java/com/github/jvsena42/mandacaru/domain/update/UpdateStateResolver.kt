package com.github.jvsena42.mandacaru.domain.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.data.update.UpdateDownloadState

/**
 * Single source-of-truth resolver for update UI state.
 *
 * Inputs:
 * - remote update availability (UpdateStatus)
 * - local download state (UpdateDownloadState)
 *
 * Output:
 * - UpdateState (UI state machine)
 */
class UpdateStateResolver(
    private val context: Context
) {

    private val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun resolve(
        status: com.github.jvsena42.mandacaru.domain.model.UpdateStatus,
        download: UpdateDownloadState?
    ): UpdateState {

        // 1. No update available
        if (!status.isUpdateAvailable) {
            return UpdateState.NoUpdate
        }

        // 2. Download exists → check DownloadManager
        if (download != null) {

            val cursor = dm.query(
                DownloadManager.Query().setFilterById(download.downloadId)
            ) ?: return UpdateState.Available

            cursor.use {
                if (!it.moveToFirst()) return UpdateState.Available

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
                            UpdateState.Available
                        }
                    }

                    else -> UpdateState.Available
                }
            }
        }

        // 3. Update exists but no download started
        return UpdateState.Available
    }
}
