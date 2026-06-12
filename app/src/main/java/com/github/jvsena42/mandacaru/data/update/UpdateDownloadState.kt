package com.github.jvsena42.mandacaru.data.update

/**
 * Tracks an active or completed APK download.
 *
 * This is persisted indirectly via PreferencesDataSource / registry,
 * and used by UpdateStateResolver to decide UI state.
 */
data class UpdateDownloadState(
    val version: String,
    val downloadId: Long,
    val isCompleted: Boolean = false,
    val uri: String? = null
) {

    fun markCompleted(localUri: String): UpdateDownloadState {
        return copy(
            isCompleted = true,
            uri = localUri
        )
    }

    fun isSameVersion(otherVersion: String): Boolean {
        return version == otherVersion
    }
}
