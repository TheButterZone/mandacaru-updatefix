sealed class UpdateState {
    data object NoUpdate : UpdateState()
    data object Downloading : UpdateState()
    data class ReadyToInstall(val uri: Uri) : UpdateState()
    data object Available : UpdateState()
}
