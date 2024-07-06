package io.github.drumber.kitsune.data.source.local.library

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.data.common.media.AgeRating
import io.github.drumber.kitsune.data.common.media.AnimeSubtype
import io.github.drumber.kitsune.data.common.media.MangaSubtype
import io.github.drumber.kitsune.data.common.media.ReleaseStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalReactionSkip
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyType
import io.github.drumber.kitsune.di.createObjectMapper

class LocalLibraryConverters {

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
    fun stringMapToString(map: Map<String, String?>?): String? {
        return if (map != null) {
            objectMapper.writeValueAsString(map)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToStringMap(mapJson: String?): Map<String, String?>? {
        return if (mapJson != null) {
            objectMapper.readValue(mapJson)
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
    fun statusToString(status: ReleaseStatus?): String? {
        return if (status != null) {
            objectMapper.writeValueAsString(status)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToStatus(json: String?): ReleaseStatus? {
        return if (json != null) {
            objectMapper.readValue(json)
        } else {
            null
        }
    }

    @TypeConverter
    fun libraryStatusToOrderId(status: LocalLibraryStatus?): Int? {
        return status?.orderId
    }

    @TypeConverter
    fun orderIdToLibraryStatus(orderId: Int?): LocalLibraryStatus? {
        return if (orderId != null) {
            LocalLibraryStatus.entries.find { it.orderId == orderId }
        } else {
            null
        }
    }

    @TypeConverter
    fun reactionSkipToString(reactionSkip: LocalReactionSkip?): String? {
        return if (reactionSkip != null) {
            objectMapper.writeValueAsString(reactionSkip)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringToReactionSkip(json: String?): LocalReactionSkip? {
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