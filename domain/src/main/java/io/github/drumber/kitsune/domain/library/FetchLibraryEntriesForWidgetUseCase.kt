package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.library.LibraryEntryMediaType
import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.data.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.shared.logE

class FetchLibraryEntriesForWidgetUseCase(
    private val libraryRepository: LibraryRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) {

    suspend operator fun invoke(count: Int) {
        val userId = getLocalUserId() ?: return

        val requestFilter = Filter()
            .filter("user_id", userId)
            .sort("status", "-progressed_at")
            .include("anime", "manga")

        val filter = LibraryEntryFilter(
            kind = LibraryEntryMediaType.All,
            libraryStatus = listOf(LibraryStatus.Current),
            initialFilter = requestFilter
        ).pageSize(count)

        try {
            libraryRepository.fetchAndStoreLibraryEntriesForFilter(filter)
        } catch (e: Exception) {
            logE("Failed to fetch library entries for widget", e)
        }
    }
}