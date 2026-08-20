package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.presentation.model.group.GroupCategory
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroup
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupCategory

object GroupMapper {

    fun NetworkGroup.toGroup(): Group {
        return Group(
            id = id.require(),
            createdAt = createdAt,
            lastActivityAt = lastActivityAt,
            name = name,
            slug = slug,
            tagline = tagline,
            about = about,
            rules = rules,
            rulesFormatted = rulesFormatted,
            privacy = privacy,
            nsfw = nsfw ?: false,
            featured = featured ?: false,
            membersCount = membersCount ?: 0,
            leadersCount = leadersCount ?: 0,
            avatarUrl = avatar?.toImage()?.largeOrDown(),
            coverImageUrl = coverImage?.toImage()?.originalOrDown(),
            categoryId = category?.id,
            categoryName = category?.name
        )
    }

    fun NetworkGroupCategory.toGroupCategory(): GroupCategory {
        return GroupCategory(
            id = id.require(),
            name = name,
            slug = slug
        )
    }

}
