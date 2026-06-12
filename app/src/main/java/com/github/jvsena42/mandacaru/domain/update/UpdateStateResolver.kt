package com.github.jvsena42.mandacaru.domain.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.github.jvsena42.mandacaru.data.update.UpdateDownloadState

class UpdateStateResolver(
    private val context: Context
) {

    private val dm = context.getSystemService(DownloadManager::class.java)

    fun resolve(state: UpdateDownloadState?): UpdateState {

        if (state == null) return UpdateState.Available

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(state.downloadId)
        ) ?: return UpdateState.Available

        cursor.use {
            if (!it.moveToFirst()) return UpdateState.Available

            val status = it.getInt(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )

            return when (status) {

                DownloadManager.STATUS_RUNNING ->
                    UpdateState.Downloading

                DownloadManager.STATUS_PAUSED ->
                    UpdateState.Downloading

                DownloadManager.STATUS_PENDING ->
                    UpdateState.Downloading

                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uriString = it.getString(
                        it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
                    )

                    val uri = Uri.parse(uriString)

                    UpdateState.ReadyToInstall(uri)
                }

                else -> UpdateState.Available
            }
        }
    }
}
