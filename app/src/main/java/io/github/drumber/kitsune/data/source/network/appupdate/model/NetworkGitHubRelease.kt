package io.github.drumber.kitsune.data.source.network.appupdate.model

import com.fasterxml.jackson.annotation.JsonProperty

data class NetworkGitHubRelease(
    @JsonProperty("tag_name")
    val version: String,
    @JsonProperty("html_url")
    val url: String,
    @JsonProperty("published_at")
    val publishDate: String
)
