package io.github.drumber.kitsune.constants

object Defaults {

    /** The minimum of required fields to display resources in a collection, e.g. in RecyclerView. */
    val MINIMUM_COLLECTION_FIELDS get() = arrayOf("slug", "titles", "canonicalTitle", "posterImage", "coverImage")

    val MINIMUM_CHARACTER_FIELDS get() = arrayOf("slug", "name", "image")

}