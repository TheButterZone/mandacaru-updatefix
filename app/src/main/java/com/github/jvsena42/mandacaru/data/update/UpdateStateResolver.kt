class UpdateStateResolver(
    private val context: Context
) {

    private val dm = context.getSystemService(DownloadManager::class.java)

    fun resolve(state: UpdateDownloadState?): UpdateState {

        if (state == null) return UpdateState.Available

        val cursor = dm.query(
            DownloadManager.Query().setFilterById(state.downloadId)
        ) ?: return UpdateState.Available

        cursor.use {
            if (!it.moveToFirst()) return UpdateState.Available

            val status = it.getInt(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )

            return when (status) {

                DownloadManager.STATUS_RUNNING ->
                    UpdateState.Downloading

                DownloadManager.STATUS_SUCCESSFUL -> {
                    val uri = Uri.parse(
                        it.getString(
                            it.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_LOCAL_URI
                            )
                        )
                    )
                    UpdateState.ReadyToInstall(uri)
                }

                else -> UpdateState.Available
            }
        }
    }
}
