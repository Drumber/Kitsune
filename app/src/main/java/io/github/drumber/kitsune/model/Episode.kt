package io.github.drumber.kitsune.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("episodes")
data class Episode(
    @Id var id: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var description: String? = null,
    var titles: Titles? = null,
    var canonicalTitle: String? = null,
    var seasonNumber: Int? = null,
    var number: Int? = null,
    var relativeNumber: Int? = null,
    var airdate: String? = null,
    var length: String? = null,
    var thumbnail: Image? = null
)
