package io.github.drumber.kitsune.data.source.network.media.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.common.media.AgeRating
import io.github.drumber.kitsune.data.source.network.media.model.category.NetworkCategory
import io.github.drumber.kitsune.data.source.network.media.model.relationship.NetworkMediaRelationship

@Type("manga")
data class NetworkManga(
    @Id
    override val id: String = "",
    override val slug: String?,

    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,
    override val abbreviatedTitles: List<String>?,

    override val averageRating: String?,
    override val ratingFrequencies: NetworkRatingFrequencies?,
    override val userCount: Int?,
    override val favoritesCount: Int?,
    override val popularityRank: Int?,
    override val ratingRank: Int?,

    override val startDate: String?,
    override val endDate: String?,
    override val nextRelease: String?,
    override val tba: String?,
    override val status: NetworkReleaseStatus?,

    override val ageRating: AgeRating?,
    override val ageRatingGuide: String?,
    override val nsfw: Boolean?,

    override val posterImage: Image?,
    override val coverImage: Image?,

    override val totalLength: Int?,
    val chapterCount: Int?,
    val volumeCount: Int?,
    val subtype: NetworkMangaSubtype?,
    val serialization: String?,

    @Relationship("categories")
    override val categories: List<NetworkCategory>?,
    @Relationship("mediaRelationships")
    override val mediaRelationships: List<NetworkMediaRelationship>?
) : NetworkMedia {

    companion object {
        fun empty(id: String) = NetworkManga(
            id = id,
            slug = null,
            description = null,
            titles = null,
            canonicalTitle = null,
            abbreviatedTitles = null,
            averageRating = null,
            ratingFrequencies = null,
            userCount = null,
            favoritesCount = null,
            popularityRank = null,
            ratingRank = null,
            startDate = null,
            endDate = null,
            nextRelease = null,
            tba = null,
            status = null,
            ageRating = null,
            ageRatingGuide = null,
            nsfw = null,
            posterImage = null,
            coverImage = null,
            totalLength = null,
            chapterCount = null,
            volumeCount = null,
            subtype = null,
            serialization = null,
            categories = null,
            mediaRelationships = null
        )
    }

}
