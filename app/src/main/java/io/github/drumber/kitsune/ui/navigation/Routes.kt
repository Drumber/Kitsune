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

    /** @param posterUrl poster of the media, carried along so the header does not depend on the
     *  media being in the user's library. */
    @Serializable
    data class Episodes(
        val mediaId: String,
        val isAnime: Boolean,
        val posterUrl: String? = null
    )

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

    @Serializable
    data object Categories
