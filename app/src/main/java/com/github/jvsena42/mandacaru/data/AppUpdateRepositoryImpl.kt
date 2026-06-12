package com.github.jvsena42.mandacaru.data.update

import android.util.Log
import com.github.jvsena42.mandacaru.BuildConfig
import com.github.jvsena42.mandacaru.data.AppUpdateRepository
import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource
import com.github.jvsena42.mandacaru.domain.model.UpdateStatus
import com.github.jvsena42.mandacaru.domain.model.github.GithubRelease
import com.github.jvsena42.mandacaru.domain.update.VersionComparator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class AppUpdateRepositoryImpl(
    private val gson: Gson,
    private val preferencesDataSource: PreferencesDataSource,
) : AppUpdateRepository {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val _updateStatus = MutableStateFlow(UpdateStatus())
    override val updateStatus = _updateStatus.asStateFlow()

    // CHANGED: Removed the '=' assignment to force a Unit return type block
    override suspend fun refresh(force: Boolean) {
        withContext(Dispatchers.IO) {
            emitCached()

            _updateStatus.update { it.copy(isChecking = true) }

            runCatching { fetch() }
                .onSuccess { apply(it) }
                .onFailure {
                    Log.w("AppUpdateRepository", "refresh failed", it)
                    _updateStatus.update { s -> s.copy(isChecking = false, checkFailed = true) }
                }
        }
    }

    // ADDED: Implemented to satisfy the abstract interface requirement
    override suspend fun markUpdateSeen() {
        // Implement tracking logic here if needed or leave empty for a stub
    }

    private suspend fun emitCached() {
        val latest = preferencesDataSource.getString(PreferenceKeys.UPDATE_LATEST_VERSION, "")
        if (latest.isEmpty()) return

        val apk = preferencesDataSource.getString(PreferenceKeys.UPDATE_LATEST_APK_URL, "")

        val isUpdate = VersionComparator.isNewer(latest, BuildConfig.VERSION_NAME)

        _updateStatus.update {
            it.copy(
                latestVersion = latest,
                apkDownloadUrl = apk.ifEmpty { null },
                isUpdateAvailable = isUpdate
            )
        }
    }

    private suspend fun apply(release: GithubRelease) {
        val latest = release.tagName.orEmpty().removePrefix("v")

        val apkUrl = release.assets
            .firstOrNull { it.name?.endsWith(".apk") == true }
            ?.browserDownloadUrl

        val isUpdate = VersionComparator.isNewer(latest, BuildConfig.VERSION_NAME)

        preferencesDataSource.setString(PreferenceKeys.UPDATE_LATEST_VERSION, latest)
        preferencesDataSource.setString(PreferenceKeys.UPDATE_LATEST_APK_URL, apkUrl.orEmpty())

        _updateStatus.update {
            it.copy(
                latestVersion = latest,
                apkDownloadUrl = apkUrl,
                releasePageUrl = release.htmlUrl ?: UpdateStatus.RELEASES_URL,
                isUpdateAvailable = isUpdate,
                isChecking = false,
                checkFailed = false
            )
        }
    }

    private fun fetch(): GithubRelease {
        val req = Request.Builder()
            .url("https://github.com")
            .header("Accept", "application/vnd.github+json")
            .build()

        client.newCall(req).execute().use {
            require(it.isSuccessful)
            return gson.fromJson(it.body.string(), GithubRelease::class.java)
        }
    }
}
