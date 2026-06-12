package com.github.jvsena42.mandacaru.domain.update

import android.net.Uri
import com.github.jvsena42.mandacaru.data.update.UpdateDownloadState
import com.github.jvsena42.mandacaru.domain.model.UpdateStatus

/**
 * Single source of truth for deciding what the UI should display
 * for update-related actions.
 *
 * This prevents UI logic from being scattered across ViewModels.
 */
sealed class UpdateState {

    data object UpToDate : UpdateState()

    data object DownloadAvailable : UpdateState()

    data object Downloading : UpdateState()

    data class ReadyToInstall(val uri: Uri) : UpdateState()

    data object Error : UpdateState()
}

/**
 * Resolves GitHub + DownloadManager state into a single UI state.
 */
class UpdateStateResolver {

    fun resolve(
        status: UpdateStatus,
        download: UpdateDownloadState?,
        downloadUri: Uri?
    ): UpdateState {

        // 1. No update available
        if (!status.isUpdateAvailable) {
            return UpdateState.UpToDate
        }

        // 2. Download completed and install-ready
        if (downloadUri != null) {
            return UpdateState.ReadyToInstall(downloadUri)
        }

        // 3. Download in progress
        if (download != null) {
            return UpdateState.Downloading
        }

        // 4. Update available but not downloaded yet
        if (!status.apkDownloadUrl.isNullOrEmpty()) {
            return UpdateState.DownloadAvailable
        }

        // 5. Fallback
        return UpdateState.Error
    }
}
