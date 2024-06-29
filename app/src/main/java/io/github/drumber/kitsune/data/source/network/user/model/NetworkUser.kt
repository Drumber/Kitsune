package io.github.drumber.kitsune.data.source.network.user.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.user.UserThemePreference

@Type("users")
data class NetworkUser(
    @Id
    val id: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null,

    val name: String? = null,
    val slug: String? = null,
    val email: String? = null,
    val title: String? = null,

    val avatar: Image? = null,
    val coverImage: Image? = null,

    val about: String? = null,
    val location: String? = null,
    val gender: String? = null,
    val birthday: String? = null,
    val waifuOrHusbando: String? = null,

    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val commentsCount: Int? = null,
    val favoritesCount: Int? = null,
    val likesGivenCount: Int? = null,
    val reviewsCount: Int? = null,
    val likesReceivedCount: Int? = null,
    val postsCount: Int? = null,
    val ratingsCount: Int? = null,
    val mediaReactionsCount: Int? = null,

    val country: String? = null,
    val language: String? = null,
    val timeZone: String? = null,
    val theme: UserThemePreference? = null,

    val sfwFilter: Boolean? = null,
    val ratingSystem: NetworkRatingSystemPreference? = null,
    val shareToGlobal: Boolean? = null,
    val sfwFilterPreference: NetworkSfwFilterPreference? = null,
    val titleLanguagePreference: NetworkTitleLanguagePreference? = null,

    val profileCompleted: Boolean? = null,
    val feedCompleted: Boolean? = null,
    val proTier: String? = null,
    val proExpiresAt: String? = null,
    val aoPro: String? = null,

    val facebookId: String? = null,
    val confirmed: Boolean? = null,
    val status: String? = null,
    val hasPassword: Boolean? = null,
    val subscribedToNewsletter: Boolean? = null,

//    @Relationship("stats")
//    val stats: List<Stats>? = null,
//    @Relationship("favorites")
//    val favorites: List<Favorite>? = null,
//    @Relationship("waifu")
//    val waifu: Character? = null,
//    @Relationship("profileLinks")
//    val profileLinks: List<ProfileLink>? = null,
)
