package com.github.jvsena42.mandacaru.domain.update

import android.net.Uri

/**
 * Final UI state machine for update system.
 *
 * Only 3 real UI states exist:
 * - Nothing to do
 * - Downloading
 * - Ready to install
 */
sealed class UpdateState {

    /**
     * No update available OR idle state
     */
    data object Idle : UpdateState()

    /**
     * Download in progress
     */
    data object Downloading : UpdateState()

    /**
     * APK is ready to install
     */
    data class ReadyToInstall(val uri: Uri) : UpdateState()
}
