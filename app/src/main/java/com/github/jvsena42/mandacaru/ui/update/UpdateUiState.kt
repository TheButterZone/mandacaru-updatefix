package com.github.jvsena42.mandacaru.ui.update

import android.net.Uri

sealed class UpdateUiState {

    data object NoUpdate : UpdateUiState()

    data object Checking : UpdateUiState()

    data object DownloadAvailable : UpdateUiState()

    data object Downloading : UpdateUiState()

    data class ReadyToInstall(
        val uri: Uri
    ) : UpdateUiState()

    data object UpToDate : UpdateUiState()

    data object Error : UpdateUiState()
}
