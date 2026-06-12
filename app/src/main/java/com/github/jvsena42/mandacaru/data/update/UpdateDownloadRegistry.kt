package com.github.jvsena42.mandacaru.data.update

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory single source of truth for ongoing and completed updates.
 * Persists only during app runtime (not across restarts).
 */
class UpdateDownloadRegistry {

    private val _activeDownload = MutableStateFlow<UpdateDownloadState?>(null)
    val activeDownload = _activeDownload.asStateFlow()

    private val _completedApkUri = MutableStateFlow<Uri?>(null)
    val completedApkUri = _completedApkUri.asStateFlow()

    fun getActiveDownloadState(): UpdateDownloadState? = _activeDownload.value

    fun getActiveDownloadId(): Long? = _activeDownload.value?.downloadId

    fun getCompletedApkUri(): Uri? = _completedApkUri.value

    fun markDownloading(version: String, downloadId: Long) {
        _activeDownload.value = UpdateDownloadState(version, downloadId)
        _completedApkUri.value = null
    }

    fun markDownloaded(version: String, uri: Uri) {
        val current = _activeDownload.value
        if (current?.version == version) {
            _activeDownload.value = current.copy(isCompleted = true)
            _completedApkUri.value = uri
        }
    }

    fun isDownloading(version: String): Boolean {
        val current = _activeDownload.value
        return current?.version == version && !current.isCompleted
    }

    fun isDownloaded(version: String): Boolean {
        val current = _activeDownload.value
        return current?.version == version && current.isCompleted
    }

    fun reset() {
        _activeDownload.value = null
        _completedApkUri.value = null
    }
}
