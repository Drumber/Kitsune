package io.github.drumber.kitsune.data.room

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.data.model.RemoteKeyType
import io.github.drumber.kitsune.data.model.library.ReactionSkip
import io.github.drumber.kitsune.data.model.media.AgeRating
import io.github.drumber.kitsune.data.model.media.AnimeSubtype
import io.github.drumber.kitsune.data.model.media.MangaSubtype
import io.github.drumber.kitsune.data.model.media.Status
import io.github.drumber.kitsune.di.createObjectMapper

class Converters {

    private val objectMapper = createObjectMapper()

    @TypeConverter
    fun stringListToString(list: List<String>?): String? {
        return if (list != null) {
            objectMapper.writeValueAsString(list)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToStringList(listJson: String?): List<String>? {
        return if (listJson != null) {
            objectMapper.readValue(listJson)
        } else {
            null
        }
    }

    @TypeConverter
    fun ageRatingToString(ageRating: AgeRating?): String? {
        return if (ageRating != null) {
            objectMapper.writeValueAsString(ageRating)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToAgeRating(json: String?): AgeRating? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

    @TypeConverter
    fun animeSubtypeToString(animeSubtype: AnimeSubtype?): String? {
        return if (animeSubtype != null) {
            objectMapper.writeValueAsString(animeSubtype)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToAnimeSubtype(json: String?): AnimeSubtype? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

    @TypeConverter
    fun mangaSubtypeToString(mangaSubtype: MangaSubtype?): String? {
        return if (mangaSubtype != null) {
            objectMapper.writeValueAsString(mangaSubtype)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToMangaSubtype(json: String?): MangaSubtype? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

    @TypeConverter
    fun statusToString(status: Status?): String? {
        return if (status != null) {
            objectMapper.writeValueAsString(status)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToStatus(json: String?): Status? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

    @TypeConverter
    fun libraryStatusToOrdinal(status: io.github.drumber.kitsune.data.model.library.Status?): Int? {
        return status?.ordinal
    }

    @TypeConverter
    fun ordinalToLibraryStatus(ordinal: Int?): io.github.drumber.kitsune.data.model.library.Status? {
        return if (ordinal != null) {
            io.github.drumber.kitsune.data.model.library.Status.values()[ordinal]
        } else {
            null
        }
    }

    @TypeConverter
    fun reactionSkipToString(reactionSkip: ReactionSkip?): String? {
        return if (reactionSkip != null) {
            objectMapper.writeValueAsString(reactionSkip)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToReactionSkip(json: String?): ReactionSkip? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

    @TypeConverter
    fun remoteKeyTypeToString(remoteKeyType: RemoteKeyType?): String? {
        return if (remoteKeyType != null) {
            objectMapper.writeValueAsString(remoteKeyType)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToRemoteKeyType(json: String?): RemoteKeyType? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

}