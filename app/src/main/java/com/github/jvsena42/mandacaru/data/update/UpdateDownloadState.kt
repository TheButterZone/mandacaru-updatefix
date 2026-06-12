package com.github.jvsena42.mandacaru.data.update

import android.net.Uri

/**
 * Single runtime source of truth for an in-progress or completed update download.
 *
 * This replaces scattered booleans like:
 * - isUpdateDownloading
 * - partial resolver inference
 *
 * UI should derive ALL download state from this.
 */
data class UpdateDownloadState(
    val version: String,
    val downloadId: Long,

    /**
     * True once DownloadManager reports success
     */
    val isCompleted: Boolean = false,

    /**
     * Local file Uri once download is finished
     */
    val uri: Uri? = null
)
