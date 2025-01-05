package io.github.drumber.kitsune.data.presentation.model.mapping

import io.github.drumber.kitsune.shared.Kitsu

data class Mapping(
    val id: String,
    val externalSite: String?,
    val externalId: String?
)

fun Mapping.getSiteName() = when (externalSite) {
    "kitsu/anime", "kitsu/manga" -> "Kitsu"
    "anidb" -> "AniDB"
    "anilist", "anilist/anime", "anilist/manga" -> "AniList"
    "myanimelist", "myanimelist/anime", "myanimelist/manga" -> "MyAnimeList"
    "thetvdb", "thetvdb/season", "thetvdb/series" -> "TheTVDB"
    "trakt" -> "Trakt"
    "hulu" -> "Hulu"
    "mangaupdates" -> "Baka-Updates Manga"
    else -> null
}

fun Mapping.getExternalUrl() = when (externalSite) {
    "kitsu/anime" -> "${Kitsu.BASE_URL}/anime/$externalId"
    "kitsu/manga" -> "${Kitsu.BASE_URL}/manga/$externalId"
    "anidb" -> "https://anidb.net/anime/$externalId"
    "anilist/anime" -> "https://anilist.co/anime/$externalId"
    "anilist/manga" -> "https://anilist.co/manga/$externalId"
    "myanimelist/anime" -> "https://myanimelist.net/anime/$externalId"
    "myanimelist/manga" -> "https://myanimelist.net/manga/$externalId"
    "thetvdb/season" -> "https://thetvdb.com/dereferrer/season/$externalId"
    "thetvdb/series" -> "https://thetvdb.com/dereferrer/series/$externalId"
    "thetvdb" -> "https://thetvdb.com/dereferrer/series/${externalId?.replace(Regex("/.*"), "")}"
    "trakt" -> "https://trakt.tv/shows/$externalId"
    "hulu" -> "https://hulu.jp/series/$externalId"
    "mangaupdates" -> "https://www.mangaupdates.com/series/$externalId"
    else -> null
}
