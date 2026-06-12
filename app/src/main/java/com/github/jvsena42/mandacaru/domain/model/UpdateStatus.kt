package com.github.jvsena42.mandacaru.domain.model

/**
 * Represents GitHub-driven update metadata only.
 *
 * IMPORTANT:
 * This model does NOT represent download state anymore.
 * Download lifecycle is handled by DownloadManager layer.
 */
data class UpdateStatus(
    val isUpdateAvailable: Boolean = false,

    /**
     * Latest semantic version from GitHub (e.g. "1.2.3")
     */
    val latestVersion: String = "",

    /**
     * Direct APK asset URL from GitHub release.
     * May be null if no APK asset is present.
     */
    val apkDownloadUrl: String? = null,

    /**
     * Optional browser fallback for release page.
     */
    val releasePageUrl: String = RELEASES_URL,

    /**
     * Whether a background check is currently running.
     */
    val isChecking: Boolean = false,

    /**
     * Whether the last update check failed.
     */
    val checkFailed: Boolean = false,

    /**
     * Badge visibility is purely a UI concern:
     * "Is there an unseen update?"
     */
    val isBadgeVisible: Boolean = false,
) {
    companion object {
        const val RELEASES_URL =
            "https://github.com/jvsena42/mandacaru/releases/latest"
    }
}
