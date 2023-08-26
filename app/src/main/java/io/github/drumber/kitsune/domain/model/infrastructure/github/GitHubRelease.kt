package io.github.drumber.kitsune.domain.model.infrastructure.github

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubRelease(
    @JsonProperty("tag_name")
    val version: String,
    @JsonProperty("html_url")
    val url: String,
    @JsonProperty("published_at")
    val publishDate: String
)
