package io.github.drumber.kitsune.constants

object SortFilter {

    const val POPULARITY = "user_count"

    const val AVERAGE_RATING = "average_rating"

    const val DATE = "start_date"

    const val RECENTLY_ADDED = "created_at"

    /** Get the sort string for descending order */
    fun String.desc() = "-$this"

}