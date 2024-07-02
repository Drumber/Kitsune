package io.github.drumber.kitsune.data.source.network.media.streamer

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("streamers")
data class NetworkStreamer(
    @Id
    val id: String?,
    val siteName: String?,
)
