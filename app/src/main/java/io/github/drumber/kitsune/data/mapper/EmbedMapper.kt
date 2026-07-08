package io.github.drumber.kitsune.data.mapper

import com.fasterxml.jackson.databind.JsonNode
import io.github.drumber.kitsune.data.presentation.model.feed.Embed
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkEmbed

object EmbedMapper {

    // Returns node's text, or null if absent/JSON null (prevents "null" string leak)
    private fun JsonNode?.asTextOrNull(): String? =
        this?.takeUnless { it.isNull }?.asText()?.takeIf { it.isNotBlank() }

    // Resolves node as bare string or object property, returns null for missing/JSON null values
    // @param objectKey the property name to extract from object nodes
    private fun JsonNode?.resolve(objectKey: String): String? {
        val node = this ?: return null
        return if (node.isTextual) node.asTextOrNull() else node.get(objectKey).asTextOrNull()
    }

    fun NetworkEmbed.toEmbed(): Embed {
        val resolvedSiteName = site.resolve("name")
        val resolvedImageUrl = image.resolve("url")
        val resolvedVideoUrl = video.resolve("url")
        val resolvedVideoType = video?.takeIf { !it.isTextual }?.get("type").asTextOrNull()
        return Embed(
            kind = kind,
            title = title,
            description = description,
            url = url,
            siteName = resolvedSiteName,
            imageUrl = resolvedImageUrl,
            videoUrl = resolvedVideoUrl,
            videoType = resolvedVideoType
        )
    }
}
