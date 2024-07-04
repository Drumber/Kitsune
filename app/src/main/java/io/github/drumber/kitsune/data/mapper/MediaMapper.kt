package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.CharacterMapper.toCharacter
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.AnimeSubtype
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.MangaSubtype
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.RatingFrequencies
import io.github.drumber.kitsune.data.presentation.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.data.presentation.model.media.production.AnimeProduction
import io.github.drumber.kitsune.data.presentation.model.media.production.AnimeProductionRole
import io.github.drumber.kitsune.data.presentation.model.media.production.Casting
import io.github.drumber.kitsune.data.presentation.model.media.production.Person
import io.github.drumber.kitsune.data.presentation.model.media.production.Producer
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationshipRole
import io.github.drumber.kitsune.data.presentation.model.media.streamer.Streamer
import io.github.drumber.kitsune.data.presentation.model.media.streamer.StreamingLink
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnimeSubtype
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMangaSubtype
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia
import io.github.drumber.kitsune.data.source.network.media.model.NetworkRatingFrequencies
import io.github.drumber.kitsune.data.source.network.media.model.NetworkReleaseStatus
import io.github.drumber.kitsune.data.source.network.media.model.category.NetworkCategory
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkAnimeProduction
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkAnimeProductionRole
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkCasting
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkPerson
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkProducer
import io.github.drumber.kitsune.data.source.network.media.model.relationship.NetworkMediaRelationship
import io.github.drumber.kitsune.data.source.network.media.model.relationship.NetworkMediaRelationshipRole
import io.github.drumber.kitsune.data.source.network.media.model.streamer.NetworkStreamer
import io.github.drumber.kitsune.data.source.network.media.model.streamer.NetworkStreamingLink

object MediaMapper {
    fun NetworkMedia.toMedia(): Media = when (this) {
        is NetworkAnime -> toAnime()
        is NetworkManga -> toManga()
    }

    fun NetworkAnime.toAnime() = Anime(
        id = id,
        slug = slug,
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies?.toRatingFrequencies(),
        userCount = userCount,
        favoritesCount = favoritesCount,
        popularityRank = popularityRank,
        ratingRank = ratingRank,
        startDate = startDate,
        endDate = endDate,
        nextRelease = nextRelease,
        tba = tba,
        status = status?.toReleaseStatus(),
        ageRating = ageRating,
        ageRatingGuide = ageRatingGuide,
        nsfw = nsfw,
        posterImage = posterImage,
        coverImage = coverImage,
        totalLength = totalLength,
        episodeCount = episodeCount,
        episodeLength = episodeLength,
        youtubeVideoId = youtubeVideoId,
        subtype = subtype?.toAnimeSubtype(),
        categories = categories?.map { it.toCategory() },
        animeProduction = animeProduction?.map { it.toAnimeProduction() },
        streamingLinks = streamingLinks?.map { it.toStreamingLink() },
        mediaRelationships = mediaRelationships?.map { it.toMediaRelationship() }
    )

    fun NetworkManga.toManga() = Manga(
        id = id,
        slug = slug,
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies?.toRatingFrequencies(),
        userCount = userCount,
        favoritesCount = favoritesCount,
        popularityRank = popularityRank,
        ratingRank = ratingRank,
        startDate = startDate,
        endDate = endDate,
        nextRelease = nextRelease,
        tba = tba,
        status = status?.toReleaseStatus(),
        ageRating = ageRating,
        ageRatingGuide = ageRatingGuide,
        nsfw = nsfw,
        posterImage = posterImage,
        coverImage = coverImage,
        totalLength = totalLength,
        chapterCount = chapterCount,
        volumeCount = volumeCount,
        subtype = subtype?.toMangaSubtype(),
        serialization = serialization,
        categories = categories?.map { it.toCategory() },
        mediaRelationships = mediaRelationships?.map { it.toMediaRelationship() }
    )

    fun NetworkRatingFrequencies.toRatingFrequencies() = RatingFrequencies(
        r2 = r2,
        r3 = r3,
        r4 = r4,
        r5 = r5,
        r6 = r6,
        r7 = r7,
        r8 = r8,
        r9 = r9,
        r10 = r10,
        r11 = r11,
        r12 = r12,
        r13 = r13,
        r14 = r14,
        r15 = r15,
        r16 = r16,
        r17 = r17,
        r18 = r18,
        r19 = r19,
        r20 = r20
    )

    fun NetworkReleaseStatus.toReleaseStatus(): ReleaseStatus = when (this) {
        NetworkReleaseStatus.Current -> ReleaseStatus.Current
        NetworkReleaseStatus.Finished -> ReleaseStatus.Finished
        NetworkReleaseStatus.TBA -> ReleaseStatus.TBA
        NetworkReleaseStatus.Unreleased -> ReleaseStatus.Unreleased
        NetworkReleaseStatus.Upcoming -> ReleaseStatus.Upcoming
    }

    fun NetworkAnimeSubtype.toAnimeSubtype(): AnimeSubtype = when (this) {
        NetworkAnimeSubtype.ONA -> AnimeSubtype.ONA
        NetworkAnimeSubtype.OVA -> AnimeSubtype.OVA
        NetworkAnimeSubtype.TV -> AnimeSubtype.TV
        NetworkAnimeSubtype.Movie -> AnimeSubtype.Movie
        NetworkAnimeSubtype.Music -> AnimeSubtype.Music
        NetworkAnimeSubtype.Special -> AnimeSubtype.Special
    }

    fun NetworkMangaSubtype.toMangaSubtype(): MangaSubtype = when (this) {
        NetworkMangaSubtype.Doujin -> MangaSubtype.Doujin
        NetworkMangaSubtype.Manga -> MangaSubtype.Manga
        NetworkMangaSubtype.Manhua -> MangaSubtype.Manhua
        NetworkMangaSubtype.Manhwa -> MangaSubtype.Manhwa
        NetworkMangaSubtype.Novel -> MangaSubtype.Novel
        NetworkMangaSubtype.Oel -> MangaSubtype.Oel
        NetworkMangaSubtype.Oneshot -> MangaSubtype.Oneshot
    }

    //********************************************************************************************//
    // Category
    //********************************************************************************************//

    fun NetworkCategory.toCategory() = Category(
        id = id.require(),
        slug = slug,
        title = title,
        description = description,
        nsfw = nsfw,
        totalMediaCount = totalMediaCount,
        childCount = childCount
    )

    //********************************************************************************************//
    // Production
    //********************************************************************************************//

    fun NetworkAnimeProduction.toAnimeProduction() = AnimeProduction(
        id = id.require(),
        role = role?.toAnimeProductionRole(),
        producer = producer?.toProducer()
    )

    fun NetworkAnimeProductionRole.toAnimeProductionRole(): AnimeProductionRole = when (this) {
        NetworkAnimeProductionRole.Licensor -> AnimeProductionRole.Licensor
        NetworkAnimeProductionRole.Producer -> AnimeProductionRole.Producer
        NetworkAnimeProductionRole.Studio -> AnimeProductionRole.Studio
    }

    fun NetworkProducer.toProducer() = Producer(
        id = id.require(),
        slug = slug,
        name = name
    )

    fun NetworkCasting.toCasting() = Casting(
        id = id.require(),
        role = role,
        voiceActor = voiceActor,
        featured = featured,
        language = language,
        character = character?.toCharacter(),
        person = person?.toPerson()
    )

    fun NetworkPerson.toPerson() = Person(
        id = id.require(),
        name = name,
        description = description,
        image = image
    )

    //********************************************************************************************//
    // Relationship
    //********************************************************************************************//

    fun NetworkMediaRelationship.toMediaRelationship() = MediaRelationship(
        id = id.require(),
        role = role?.toMediaRelationshipRole(),
        media = media?.toMedia()
    )

    fun NetworkMediaRelationshipRole.toMediaRelationshipRole(): MediaRelationshipRole =
        when (this) {
            NetworkMediaRelationshipRole.Sequel -> MediaRelationshipRole.Sequel
            NetworkMediaRelationshipRole.Prequel -> MediaRelationshipRole.Prequel
            NetworkMediaRelationshipRole.AlternativeSetting -> MediaRelationshipRole.AlternativeSetting
            NetworkMediaRelationshipRole.AlternativeVersion -> MediaRelationshipRole.AlternativeVersion
            NetworkMediaRelationshipRole.SideStory -> MediaRelationshipRole.SideStory
            NetworkMediaRelationshipRole.ParentStory -> MediaRelationshipRole.ParentStory
            NetworkMediaRelationshipRole.Summary -> MediaRelationshipRole.Summary
            NetworkMediaRelationshipRole.FullStory -> MediaRelationshipRole.FullStory
            NetworkMediaRelationshipRole.Spinoff -> MediaRelationshipRole.Spinoff
            NetworkMediaRelationshipRole.Adaptation -> MediaRelationshipRole.Adaptation
            NetworkMediaRelationshipRole.Character -> MediaRelationshipRole.Character
            NetworkMediaRelationshipRole.Other -> MediaRelationshipRole.Other
        }

    //********************************************************************************************//
    // Streamer
    //********************************************************************************************//

    fun NetworkStreamingLink.toStreamingLink() = StreamingLink(
        id = id.require(),
        url = url,
        subs = subs,
        dubs = dubs,
        streamer = streamer?.toStreamer()
    )

    fun NetworkStreamer.toStreamer() = Streamer(
        id = id.require(),
        siteName = siteName
    )
}