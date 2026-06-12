package com.github.jvsena42.mandacaru.data.update

data class UpdateDownloadState(
    val version: String,
    val downloadId: Long,
    val isCompleted: Boolean = false,
    val uri: String? = null
)
