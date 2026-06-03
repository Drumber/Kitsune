package io.github.drumber.kitsune.data.source.network.feed.model

import com.fasterxml.jackson.databind.JsonNode

/**
 * Server-resolved embed data for a link in post/comment content (e.g. Tenor GIF, YouTube video,
 * Twitter card). Produced by the Kitsu server's embed service from a detected URL.
 *
 * [site], [image] and [video] may each be serialized either as an object (e.g. `{ url, type }`)
 * or as a bare string URL depending on which embedder matched, so they are captured as raw
 * [JsonNode]s and normalized in the mapper.
 */
data class NetworkEmbed(
    val kind: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val site: JsonNode? = null,
    val image: JsonNode? = null,
    val video: JsonNode? = null
)

data class NetworkEmbedMedia(
    val url: String? = null,
    val type: String? = null
)
