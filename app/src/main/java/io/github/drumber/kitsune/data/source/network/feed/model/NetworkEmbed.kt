package io.github.drumber.kitsune.data.source.network.feed.model

import com.fasterxml.jackson.databind.JsonNode

/**
 * Server-resolved embed data for a link in post/comment content (e.g. Tenor GIF, YouTube video,
 * Twitter card). Produced by the Kitsu server's embed service from a detected URL.
 *
 * [site] may be serialized either as an object (`{ name, url }`) or as a bare string depending on
 * which embedder matched, so it is captured as a raw [JsonNode] and normalized in the mapper.
 */
data class NetworkEmbed(
    val kind: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val site: JsonNode? = null,
    val image: NetworkEmbedMedia? = null,
    val video: NetworkEmbedMedia? = null
)

data class NetworkEmbedMedia(
    val url: String? = null,
    val type: String? = null
)
