package com.github.jvsena42.mandacaru.domain.model

data class UpdateStatus(
    val isUpdateAvailable: Boolean = false,
    val latestVersion: String = "",
    val apkDownloadUrl: String? = null,
    val releasePageUrl: String = RELEASES_URL,
    val isChecking: Boolean = false,
    val checkFailed: Boolean = false,
    val isBadgeVisible: Boolean = false,
) {
    companion object {
        const val RELEASES_URL = "https://github.com/jvsena42/mandacaru/releases/latest"
    }
}
