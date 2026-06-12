package com.github.jvsena42.mandacaru.domain.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.domain.model.UpdateStatus

/**
 * Converts raw DownloadManager state + app update info into UI-friendly states.
 *
 * This drives:
 * - "Download" button
 * - "Downloading" progress
 * - "Install" button visibility
 */
class UpdateStateResolver(
    private val context: Context
) {

    private val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /**
     * @param status current update status from repository
     * @param downloadId active DownloadManager download ID, if any
     */
    fun resolve(
        status: UpdateStatus,
        downloadId: Long?
    ): UpdateState {

        // 1. No update available at all
        if (!status.isUpdateAvailable) {
            return UpdateState.Idle
        }

        // 2. No active download yet
        if (downloadId == null) {
            return UpdateState.Available
        }

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(downloadId)
        ) ?: return UpdateState.Available

        cursor.use {
            if (!it.moveToFirst()) return UpdateState.Available

            val statusIndex = it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            val dmStatus = it.getInt(statusIndex)

            return when (dmStatus) {

                DownloadManager.STATUS_RUNNING ->
                    UpdateState.Downloading

                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uriIndex = it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
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
