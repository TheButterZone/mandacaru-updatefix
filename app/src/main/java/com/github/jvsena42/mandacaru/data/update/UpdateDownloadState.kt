package com.github.jvsena42.mandacaru.data.update

import android.net.Uri

/**
 * Runtime representation of an active or completed APK download.
 * Single source of truth for download tracking.
 */
data class UpdateDownloadState(
    val version: String,
    val downloadId: Long,
    val isCompleted: Boolean = false,
    val uri: Uri? = null
)
