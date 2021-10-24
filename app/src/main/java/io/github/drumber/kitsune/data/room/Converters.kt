package io.github.drumber.kitsune.data.room

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.drumber.kitsune.data.model.resource.AgeRating
import io.github.drumber.kitsune.data.model.resource.AnimeSubtype
import io.github.drumber.kitsune.data.model.resource.MangaSubtype
import io.github.drumber.kitsune.data.model.resource.Status
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

}