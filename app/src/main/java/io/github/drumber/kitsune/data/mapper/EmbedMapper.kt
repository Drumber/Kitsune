package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.feed.Embed
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkEmbed

object EmbedMapper {

    fun NetworkEmbed.toEmbed(): Embed {
        val resolvedSiteName = site?.let { node ->
            if (node.isTextual) node.asText() else node.get("name")?.asText()
        }
        val resolvedImageUrl = image?.let { node ->
            if (node.isTextual) node.asText() else node.get("url")?.asText()
        }
        val resolvedVideoUrl = video?.let { node ->
            if (node.isTextual) node.asText() else node.get("url")?.asText()
        }
        val resolvedVideoType = video?.takeIf { !it.isTextual }?.get("type")?.asText()
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