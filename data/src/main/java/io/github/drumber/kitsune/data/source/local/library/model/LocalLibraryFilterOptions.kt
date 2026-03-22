package io.github.drumber.kitsune.data.source.local.library.model

data class LocalLibraryFilterOptions(
    val mediaType: String?,
    val status: List<String>?,
    val sortBy: String?,
    val sortDirection: String?
) {
    fun serialize(): String {
        return listOfNotNull(
            mediaType,
            status?.ifEmpty { null }?.joinToString("-"),
            sortBy,
            sortDirection
        ).joinToString("_") { it.lowercase() }
    }
}
