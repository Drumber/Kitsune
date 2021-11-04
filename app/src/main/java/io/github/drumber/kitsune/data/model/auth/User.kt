package io.github.drumber.kitsune.data.model.auth

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.TitlesPref
import io.github.drumber.kitsune.data.model.resource.Image
import io.github.drumber.kitsune.data.model.stats.Stats
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("users")
data class User(
    @Id val id: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val about: String? = null,
    val location: String? = null,
    val waifuOrHusbando: String? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val birthday: String? = null,
    val gender: String? = null,
    val commentsCount: Int? = null,
    val favoritesCount: Int? = null,
    val likesGivenCount: Int? = null,
    val reviewsCount: Int? = null,
    val likesReceivedCount: Int? = null,
    val postsCount: Int? = null,
    val ratingsCount: Int? = null,
    val mediaReactionsCount: Int? = null,
    val proExpiresAt: String? = null,
    val title: String? = null,
    val profileCompleted: Boolean? = null,
    val feedCompleted: Boolean? = null,
    val proTier: String? = null,
    val avatar: Image? = null,
    val coverImage: Image? = null,
    val email: String? = null,
    val confirmed: Boolean? = null,
    val language: String? = null,
    val timeZone: String? = null,
    val country: String? = null,
    val shareToGlobal: Boolean? = null,
    val titleLanguagePreference: TitlesPref? = null,
    val sfwFilter: Boolean? = null,
    val ratingSystem: RatingSystem? = null,
    val theme: Theme? = null,
    val facebookId: String? = null,
    val hasPassword: Boolean? = null,
    val status: String? = null,
    val subscribedToNewsletter: Boolean? = null,
    val aoPro: String? = null,
    @Relationship("stats")
    val stats: List<Stats>? = null
) : Parcelable

enum class RatingSystem {
    // 0.5, 1...10
    Advanced,
    // 0.5, 1...5
    Regular,
    // :(, :|, :), :D
    Simple
}

enum class Theme {
    Dark, Light
}
