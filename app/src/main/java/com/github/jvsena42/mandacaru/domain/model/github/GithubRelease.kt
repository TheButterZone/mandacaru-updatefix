package com.github.jvsena42.mandacaru.domain.model.github

import com.google.gson.annotations.SerializedName

data class GithubRelease(
    @SerializedName("tag_name") val tagName: String?,
    @SerializedName("html_url") val htmlUrl: String?,
    val assets: List<GithubAsset> = emptyList(),
)

data class GithubAsset(
    val name: String?,
    @SerializedName("browser_download_url") val browserDownloadUrl: String?,
)
