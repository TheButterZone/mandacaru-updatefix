fun recoverDownloadState(
    context: Context
): UpdateDownloadState? {

    val prefs = context.getSharedPreferences("updates", Context.MODE_PRIVATE)

    val version = prefs.getString("version", null)
    val id = prefs.getLong("downloadId", -1L)

    if (version != null && id != -1L) {
        return UpdateDownloadState(version, id)
    }

    // fallback: try to recover from DownloadManager only
    val dm = context.getSystemService(DownloadManager::class.java)

    val cursor = dm.query(
        DownloadManager.Query()
            .setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
    ) ?: return null

    cursor.use {
        val idIndex = it.getColumnIndex(DownloadManager.COLUMN_ID)

        while (it.moveToNext()) {
            val downloadId = it.getLong(idIndex)
            return UpdateDownloadState("unknown", downloadId)
        }
    }

    return null
}
