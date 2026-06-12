package com.github.jvsena42.mandacaru.domain.model

/**
 * Pure GitHub-derived update metadata.
 *
 * IMPORTANT:
 * This model MUST NOT contain download lifecycle state.
 * Downloading / installing is handled by DownloadManager layer.
 */
data class UpdateStatus(
    val isUpdateAvailable: Boolean = false,

    /**
     * Latest version string from GitHub (e.g. "1.2.3")
     */
    val latestVersion: String = "",

    /**
     * Direct APK asset URL (nullable if no APK exists in release)
     */
    val apkDownloadUrl: String? = null,

    /**
     * Fallback browser URL for release page
     */
    val releasePageUrl: String = RELEASES_URL,

    /**
     * Whether update check is currently running
     */
    val isChecking: Boolean = false,

    /**
     * Whether last update check failed
     */
    val checkFailed: Boolean = false,

    /**
     * UI badge visibility (unseen update indicator)
     */
    val isBadgeVisible: Boolean = false,
) {
    companion object {
        const val RELEASES_URL =
            "https://github.com/jvsena42/mandacaru/releases/latest"
    }
}
