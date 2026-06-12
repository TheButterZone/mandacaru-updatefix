package com.github.jvsena42.mandacaru.presentation.ui.screens.settings

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jvsena42.mandacaru.data.AppUpdateRepository
import com.github.jvsena42.mandacaru.data.FlorestaRpc
import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource
import com.github.jvsena42.mandacaru.data.update.UpdateDownloadRegistry
import com.github.jvsena42.mandacaru.domain.scan.DescriptorQrScanner
import com.github.jvsena42.mandacaru.domain.update.UpdateState
import com.github.jvsena42.mandacaru.domain.update.UpdateStateResolver
import com.github.jvsena42.mandacaru.presentation.utils.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.florestad.Network as FlorestaNetwork

class SettingsViewModel(
    private val florestaRpc: FlorestaRpc,
    private val preferencesDataSource: PreferencesDataSource,
    private val appUpdateRepository: AppUpdateRepository,
    private val descriptorScanner: DescriptorQrScanner,
    private val updateRegistry: UpdateDownloadRegistry,
    @field:SuppressLint("StaticFieldLeak") private val context: Context,
) : ViewModel(), EventFlow<SettingsEvents> by EventFlowImpl() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val updateResolver = UpdateStateResolver(context, updateRegistry)

    init {
        viewModelScope.launch {
            appUpdateRepository.refresh(force = true)
        }

        observeUpdateStatus()
    }

    private fun observeUpdateStatus() {

        viewModelScope.launch {
            appUpdateRepository.updateStatus.collect { status ->

                val resolved = updateResolver.resolve(
                    status = status,
                    downloadId = updateRegistry.getActiveDownloadId()
                )

                _uiState.update {
                    it.copy(
                        updateStatus = status,
                        updateUiState = resolved,
                        isUpdateDownloading = resolved is UpdateState.Downloading
                    )
                }

                if (resolved is UpdateState.ReadyToInstall) {
                    viewModelScope.sendEvent(
                        SettingsEvents.OpenInstallPrompt(resolved.uri)
                    )
                }
            }
        }
    }

    fun getUpdate() {
        val status = _uiState.value.updateStatus
        val url = status.apkDownloadUrl ?: return
        val version = status.latestVersion

        viewModelScope.launch {

            if (updateRegistry.isDownloaded(version)) return@launch
            if (updateRegistry.isDownloading(version)) return@launch

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Mandacaru update $version")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(true)
                .setDestinationInExternalFilesDir(
                    context,
                    "updates",
                    "mandacaru-$version.apk"
                )

            val id = dm.enqueue(request)

            updateRegistry.markDownloading(version, id)
        }
    }

    // ---- rest unchanged ----
}
