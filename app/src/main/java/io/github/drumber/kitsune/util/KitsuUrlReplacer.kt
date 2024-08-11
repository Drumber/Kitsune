package io.github.drumber.kitsune.util

/**
 * Replaces the media URL from the old kitsu.io domain to the new kitsu.app domain.
 *
 * Added on 2024-08-11 due to sudden domain change. Algolia search results are still using the old media domain.
 * Related PR: https://github.com/Drumber/Kitsune/pull/57
 *
 * TODO: Can be removed once Kitsu has fully migrated to the new domain.
 */
fun String.fixImageUrl() = replaceFirst("media.kitsu.io", "media.kitsu.app")