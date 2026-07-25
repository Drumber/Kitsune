package io.github.drumber.kitsune.ui.navigation

import io.github.drumber.kitsune.data.common.FilterOptions
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.data.repository.FollowListType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Typed routes of the app, replacing `main_nav_graph.xml` / `settings_nav_graph.xml`.
 *
 * Arguments are deliberately restricted to primitives: screens that previously received a whole
 * Parcelable (media, post, character) now receive its id and let their ViewModel load the object,
 * which is what those ViewModels already did right after showing the passed object as a preview.
 */
object Routes {

    // ----- top level destinations (navigation bar / rail) -----

    @Serializable
    data object Home

    @Serializable
    data object Feed

    @Serializable
    data object Library

    @Serializable
    data object MyProfile

    // ----- media -----

    /**
     * Media details. Either [mediaId] (+ [isAnime]) or the deep-link pair [type] / [slug] is set.
     */
    @Serializable
    data class Details(
        val mediaId: String? = null,
        val isAnime: Boolean? = null,
        val type: String? = null,
        val slug: String? = null
    )

    /** @param filterOptionsJson JSON encoded [FilterOptions] (a `Map<String, String>`). */
    @Serializable
    data class MediaList(
        val title: String,
        val mediaType: String,
        val requestType: String,
        val filterOptionsJson: String
    )

    @Serializable
    data class Episodes(val mediaId: String, val isAnime: Boolean)

    @Serializable
    data class Characters(val mediaId: String, val isAnime: Boolean)

    @Serializable
    data class MediaFeed(val mediaId: String, val isAnime: Boolean)

    @Serializable
    data class Reactions(val mediaId: String, val isAnime: Boolean)

    // ----- search -----

    @Serializable
    data class Search(val focusSearch: Boolean = false)

    @Serializable
    data object Facet

    // ----- library -----

    @Serializable
    data class LibraryEditEntry(val libraryEntryId: String)

    // ----- social -----

    @Serializable
    data class PostDetail(val postId: String)

    @Serializable
    data class Replies(val postId: String, val parentCommentId: String)

    @Serializable
    data class ReactionDetail(val reactionId: String)

    @Serializable
    data object Groups

    @Serializable
    data class GroupDetail(val groupId: String)

    @Serializable
    data class CreatePost(
        val editPostId: String? = null,
        val targetUserId: String? = null,
        val targetUserName: String? = null,
        val targetGroupId: String? = null,
        val targetGroupName: String? = null
    )

    @Serializable
    data object Notifications

    // ----- profile -----

    @Serializable
    data class UserProfile(val userId: String, val userName: String? = null)

    @Serializable
    data class FollowList(
        val userId: String,
        val listType: String,
        val userName: String? = null
    )

    @Serializable
    data object EditProfile

    // ----- settings (nested graph) -----

    @Serializable
    data object SettingsGraph

    @Serializable
    data object Settings

    @Serializable
    data object Appearance

    @Serializable
    data object OSLibraries

    @Serializable
    data object AppLogs

    // ----- utilities -----

    @Serializable
    data class WebView(val url: String)
}

private val routeJson = Json { ignoreUnknownKeys = true }

fun MediaSelector.toMediaListRoute(title: String) = Routes.MediaList(
    title = title,
    mediaType = mediaType.name,
    requestType = requestType.name,
    filterOptionsJson = routeJson.encodeToString<Map<String, String>>(filterOptions)
)

fun Routes.MediaList.toMediaSelector(): MediaSelector {
    val options: FilterOptions = routeJson
        .decodeFromString<Map<String, String>>(filterOptionsJson)
        .toMutableMap()
    return MediaSelector(
        mediaType = MediaType.valueOf(mediaType),
        filterOptions = options,
        requestType = RequestType.valueOf(requestType)
    )
}

fun Routes.FollowList.followListType() = FollowListType.valueOf(listType)

fun FollowListType.routeValue() = name
