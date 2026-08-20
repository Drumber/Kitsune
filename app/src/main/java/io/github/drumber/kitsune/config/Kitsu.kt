package io.github.drumber.kitsune.config

object Kitsu {

    const val DEFAULT_PAGE_OFFSET = 0
    const val DEFAULT_PAGE_SIZE = 10
    const val DEFAULT_PAGE_SIZE_LIBRARY = 30

    /** Number of replies eagerly loaded as a preview under each top-level comment. */
    const val DEFAULT_REPLY_PREVIEW_SIZE = 2

    const val ALGOLIA_APP_ID = "AWQO5J657S"

    const val API_HOST = "kitsu.app"
    const val API_URL = "https://$API_HOST/api/edge/"
    const val OAUTH_URL = "https://$API_HOST/api/oauth/"
    const val BASE_URL = "https://$API_HOST"
    const val USER_URL_PREFIX = "$BASE_URL/users/"
    const val ANIME_URL_PREFIX = "$BASE_URL/anime/"

    const val MANGA_URL_PREFIX = "$BASE_URL/manga/"

    private const val DB_REQUEST_BASE_URL = "https://www.kitsu-stuff.com"
    const val ANIME_DB_REQUEST_URL = "$DB_REQUEST_BASE_URL/anime-db-request"
    const val OPEN_ANIME_REQUESTS_URL = "$DB_REQUEST_BASE_URL/open-anime-requests"
    const val MANGA_DB_REQUEST_URL = "$DB_REQUEST_BASE_URL/manga-db-request"
    const val OPEN_MANGA_REQUESTS_URL = "$DB_REQUEST_BASE_URL/open-manga-requests"
}
