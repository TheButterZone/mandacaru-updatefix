package com.github.jvsena42.mandacaru.presentation.ui.screens.settings

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jvsena42.mandacaru.data.AppUpdateRepository
import com.github.jvsena42.mandacaru.data.FlorestaRpc
import com.github.jvsena42.mandacaru.data.PreferenceKeys
import com.github.jvsena42.mandacaru.data.PreferencesDataSource
import com.github.jvsena42.mandacaru.domain.model.florestaRPC.AddNodeCommand
import com.github.jvsena42.mandacaru.domain.scan.DescriptorQrScanner
import com.github.jvsena42.mandacaru.domain.scan.DescriptorScanState
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
    @field:SuppressLint("StaticFieldLeak") private val context: Context,
) : ViewModel(), EventFlow<SettingsEvents> by EventFlowImpl() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private var nodeAddressValidationJob: Job? = null
    private var descriptorScanErrorJob: Job? = null
    private var rescanPollJob: Job? = null
    private var wasRescanning = false

    init {
        viewModelScope.launch {
            val birthdayYear = preferencesDataSource
                .getString(PreferenceKeys.WALLET_BIRTHDAY_YEAR, "")
                .toIntOrNull()
                ?: WalletBirthday.defaultYear()

            val useAlsoMobileData = preferencesDataSource
                .getBoolean(PreferenceKeys.USE_ALSO_MOBILE_DATA, false)

            val enableAdvancedFeatures = preferencesDataSource
                .getBoolean(PreferenceKeys.ENABLE_ADVANCED_FEATURES, false)

            _uiState.update {
                it.copy(
                    selectedNetwork = preferencesDataSource.getString(
                        PreferenceKeys.CURRENT_NETWORK,
                        FlorestaNetwork.BITCOIN.name
                    ),
                    walletBirthdayYear = birthdayYear,
                    useAlsoMobileData = useAlsoMobileData,
                    enableAdvancedFeatures = enableAdvancedFeatures,
                )
            }

            updateElectrumAddress()
        }

        getDescriptors()
        observeUpdateStatus()
        observeRescanState()
    }

    // ---------------------------------------------------------
    // UPDATE SYSTEM (SINGLE SOURCE OF TRUTH: DownloadManager)
    // ---------------------------------------------------------
    private fun observeUpdateStatus() {

        viewModelScope.launch {
            appUpdateRepository.refresh(force = true)
        }

        viewModelScope.launch {
            appUpdateRepository.updateStatus.collect { status ->
                _uiState.update {
                    it.copy(updateStatus = status)
                }
            }
        }
    }

    private fun getUpdate() {
        val status = _uiState.value.updateStatus
        val url = status.apkDownloadUrl ?: return
        val version = status.latestVersion

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Mandacaru update $version")
            .setDescription("Downloading update")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setMimeType("application/vnd.android.package-archive")

        dm.enqueue(request)

        Log.d("SettingsViewModel", "Update download started: $version")
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            appUpdateRepository.refresh(force = true)
        }
    }

    // ---------------------------------------------------------
    // INSTALL BUTTON LOGIC (IMPORTANT)
    // ---------------------------------------------------------
    private fun getInstallIntent(downloadId: Long): Uri? {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)

        dm.query(query)?.use { cursor ->
            if (!cursor.moveToFirst()) return null

            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )

            if (status != DownloadManager.STATUS_SUCCESSFUL) return null

            val uriString = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            )

            return Uri.parse(uriString)
        }

        return null
    }

    // ---------------------------------------------------------
    // ALL OTHER LOGIC (UNCHANGED)
    // ---------------------------------------------------------
    private fun observeRescanState() { /* unchanged */ }

    fun onAction(action: SettingsAction) { /* unchanged */ }

    private fun handleAdvancedFeaturesToggled(action: SettingsAction.OnToggleAdvancedFeatures) { /* unchanged */ }

    private fun toggleDataUsageExpanded() { /* unchanged */ }

    private fun handleMobileDataToggled(action: SettingsAction.OnToggleMobileData) { /* unchanged */ }

    private fun toggleAboutExpanded() { /* unchanged */ }

    private fun applyBirthdayYearAndRestart() { /* unchanged */ }

    fun handleNetworkSelected(action: SettingsAction.OnNetworkSelected) { /* unchanged */ }

    private suspend fun updateElectrumAddress() { /* unchanged */ }

    private fun getDescriptors() { /* unchanged */ }

    private fun debouncedValidateNodeAddress() { /* unchanged */ }

    private fun connectNode() { /* unchanged */ }

    private fun updateDescriptor() { /* unchanged */ }

    private fun loadDescriptorString(input: String, onSuccess: () -> Unit = {}) { /* unchanged */ }

    private fun openDescriptorScanner() { /* unchanged */ }

    private fun closeDescriptorScanner() { /* unchanged */ }

    private fun handleDescriptorFrame(raw: String) { /* unchanged */ }

    private fun onDescriptorScanned(descriptor: String) { /* unchanged */ }

    private fun confirmScannedDescriptor() { /* unchanged */ }

    private fun showDescriptorScanError(reason: String) { /* unchanged */ }

    private fun rescan() { /* unchanged */ }

    private fun exportLogs() { /* unchanged */ }

    override fun onCleared() {
        super.onCleared()
        nodeAddressValidationJob?.cancel()
        descriptorScanErrorJob?.cancel()
    }
}
