package io.github.drumber.kitsune.constants

import io.github.drumber.kitsune.R

enum class SortFilter(val queryParam: String) {

    POPULARITY_DESC("-user_count"),
    POPULARITY_ASC("user_count"),

    AVERAGE_RATING_DESC("-average_rating"),
    AVERAGE_RATING_ASC("average_rating"),

    DATE_DESC("-start_date"),
    DATE_ASC("start_date");

    companion object {
        fun fromQueryParam(queryParam: String?): SortFilter? {
            if(queryParam != null) {
                entries.forEach {
                    if(it.queryParam.startsWith(queryParam)) {
                        return it
                    }
                }
            }
            return null
        }
    }

}

enum class CategorySortFilter(val queryParam: String) {
    TOTAL_MEDIA_COUNT_DESC("-total_media_count"),
    TOTAL_MEDIA_COUNT_ASC("total_media_count")
}

fun SortFilter.toStringRes() = when (this) {
    SortFilter.POPULARITY_DESC -> R.string.sort_popularity_desc
    SortFilter.POPULARITY_ASC -> R.string.sort_popularity_asc
    SortFilter.AVERAGE_RATING_DESC -> R.string.sort_average_rating_desc
    SortFilter.AVERAGE_RATING_ASC -> R.string.sort_average_rating_asc
    SortFilter.DATE_DESC -> R.string.sort_release_date_desc
    SortFilter.DATE_ASC -> R.string.sort_release_date_asc
}
