package io.github.drumber.kitsune.data.model.auth

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.TitlesPref
import io.github.drumber.kitsune.data.model.resource.Image
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("users")
data class User(
    @Id val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val name: String?,
    val slug: String?,
    val about: String?,
    val location: String?,
    val waifuOrHusbando: String?,
    val followersCount: Int?,
    val followingCount: Int?,
    val birthday: String?,
    val gender: String?,
    val commentsCount: Int?,
    val favoritesCount: Int?,
    val likesGivenCount: Int?,
    val reviewsCount: Int?,
    val likesReceivedCount: Int?,
    val postsCount: Int?,
    val ratingsCount: Int?,
    val mediaReactionsCount: Int?,
    val proExpiresAt: String?,
    val title: String?,
    val profileCompleted: Boolean?,
    val feedCompleted: Boolean?,
    val proTier: String?,
    val avatar: Image?,
    val coverImage: Image?,
    val email: String?,
    val confirmed: Boolean?,
    val language: String?,
    val timeZone: String?,
    val country: String?,
    val shareToGlobal: Boolean?,
    val titleLanguagePreference: TitlesPref?,
    val sfwFilter: Boolean?,
    val ratingSystem: RatingSystem?,
    val theme: Theme?,
    val facebookId: String?,
    val hasPassword: Boolean?,
    val status: String?,
    val subscribedToNewsletter: Boolean?,
    val aoPro: String?
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
