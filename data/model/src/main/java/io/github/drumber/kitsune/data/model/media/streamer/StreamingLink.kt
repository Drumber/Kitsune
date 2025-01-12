package io.github.drumber.kitsune.data.model.media.streamer

data class StreamingLink(
    val id: String,
    val url: String?,
    val subs: List<String>?,
    val dubs: List<String>?,

    val streamer: Streamer?
)
