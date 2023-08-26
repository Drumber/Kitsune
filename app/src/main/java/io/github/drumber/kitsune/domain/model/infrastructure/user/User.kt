package io.github.drumber.kitsune.domain.model.infrastructure.user

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.user.stats.Stats
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("users")
data class User(
    @Id
    val id: String?,
    val createdAt: String?,
    val updatedAt: String?,

    val name: String?,
    val slug: String?,
    val email: String?,
    val title: String?,

    val avatar: Image?,
    val coverImage: Image?,

    val about: String?,
    val location: String?,
    val gender: String?,
    val birthday: String?,
    val waifuOrHusbando: String?,

    val followersCount: Int?,
    val followingCount: Int?,
    val commentsCount: Int?,
    val favoritesCount: Int?,
    val likesGivenCount: Int?,
    val reviewsCount: Int?,
    val likesReceivedCount: Int?,
    val postsCount: Int?,
    val ratingsCount: Int?,
    val mediaReactionsCount: Int?,

    val country: String?,
    val language: String?,
    val timeZone: String?,
    val theme: ThemePreference?,

    val sfwFilter: Boolean?,
    val ratingSystem: RatingSystemPreference?,
    val shareToGlobal: Boolean?,
    val sfwFilterPreference: SfwFilterPreference?,
    val titleLanguagePreference: TitleLanguagePreference?,

    val profileCompleted: Boolean?,
    val feedCompleted: Boolean?,
    val proTier: String?,
    val proExpiresAt: String?,
    val aoPro: String?,

    val facebookId: String?,
    val confirmed: Boolean?,
    val status: String?,
    val hasPassword: Boolean?,
    val subscribedToNewsletter: Boolean?,

    @Relationship("stats")
    val stats: List<Stats>?,
    @Relationship("favorites")
    val favorites: List<Favorite>?
) : Parcelable

