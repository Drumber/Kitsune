package io.github.drumber.kitsune.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("anime")
data class Anime(
    @Id var id: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var slug: String? = null,
    var description: String? = null,
    var titles: Titles? = null,
    var canonicalTitle: String? = null,
    var abbreviatedTitles: List<String>? = null,
    var averageRating: String? = null,
    var ratingFrequencies: Rating? = null,
    var userCount: Int? = null,
    var favoritesCount: Int? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var popularityRank: Int? = null,
    var ratingRank: Int? = null,
    var ageRating: AgeRating? = null,
    var ageRatingGuide: String? = null,
    var subtype: Subtype? = null,
    var status: Status? = null,
    var tba: String? = null,
    var posterImage: Image? = null,
    var coverImage: Image? = null,
    var episodeCount: Int? = null,
    var episodeLength: Int? = null,
    var youtubeVideoId: String? = null,
    var nsfw: Boolean? = null
)

data class Titles(
    @JsonProperty("en") var en: String? = null,
    @JsonProperty("en_jp") var enJp: String? = null,
    @JsonProperty("ja_jp") var jaJp: String? = null
)

data class Rating(
    @JsonProperty("2") var r2: String? = null,
    @JsonProperty("3") var r3: String? = null,
    @JsonProperty("4") var r4: String? = null,
    @JsonProperty("5") var r5: String? = null,
    @JsonProperty("6") var r6: String? = null,
    @JsonProperty("7") var r7: String? = null,
    @JsonProperty("8") var r8: String? = null,
    @JsonProperty("9") var r9: String? = null,
    @JsonProperty("10") var r10: String? = null,
    @JsonProperty("11") var r11: String? = null,
    @JsonProperty("12") var r12: String? = null,
    @JsonProperty("13") var r13: String? = null,
    @JsonProperty("14") var r14: String? = null,
    @JsonProperty("15") var r15: String? = null,
    @JsonProperty("16") var r16: String? = null,
    @JsonProperty("17") var r17: String? = null,
    @JsonProperty("18") var r18: String? = null,
    @JsonProperty("19") var r19: String? = null,
    @JsonProperty("20") var r20: String? = null,
)

enum class AgeRating {
    G,
    PG,
    R,
    R18
}

enum class Subtype {
    ONA,
    OVA,
    TV,
    movie,
    music,
    special
}

enum class Status {
    current,
    finished,
    tba,
    unreleased,
    upcoming
}

data class Image(
    var tiny: String? = null,
    var small: String? = null,
    var medium: String? = null,
    var large: String? = null,
    var original: String? = null,
    var meta: Meta? = null,
)

data class Meta(var dimensions: Dimensions? = null)

data class Dimensions(
    var tiny: Dimension? = null,
    var small: Dimension? = null,
    var medium: Dimension? = null,
    var large: Dimension? = null
)

data class Dimension(var width: String? = null, var height: String? = null)
