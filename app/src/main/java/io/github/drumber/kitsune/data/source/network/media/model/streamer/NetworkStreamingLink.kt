package io.github.drumber.kitsune.data.source.network.media.model.streamer

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("streamingLinks")
data class NetworkStreamingLink(
    @Id
    val id: String?,
    val url: String?,
    val subs: List<String>?,
    val dubs: List<String>?,

    @Relationship("streamer")
    val streamer: NetworkStreamer?
)
