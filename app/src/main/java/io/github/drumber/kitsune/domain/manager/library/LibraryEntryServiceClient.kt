package io.github.drumber.kitsune.domain.manager.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.Anime
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain.model.infrastructure.media.Manga
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.NotFoundException
import io.github.drumber.kitsune.util.logE
import retrofit2.HttpException

class LibraryEntryServiceClient(
    private val libraryEntriesService: LibraryEntriesService
) {

    private val filterForFullLibraryEntry
        get() = Filter().include("anime", "manga")

    suspend fun postNewLibraryEntry(
        userId: String,
        media: BaseMedia,
        status: LibraryStatus
    ): LibraryEntry? {
        val libraryEntry = LibraryEntry.withNulls().copy(
            user = User(id = userId),
            status = status,
            anime = if (media is Anime) media else null,
            manga = if (media is Manga) media else null
        )

        return try {
            libraryEntriesService.postLibraryEntry(
                JSONAPIDocument(libraryEntry),
                filterForFullLibraryEntry.options
            ).get()
        } catch (e: Exception) {
            logE("Failed to create new library entry.", e)
            null
        }
    }

    suspend fun updateLibraryEntryWithModification(
        libraryEntryModification: LocalLibraryEntryModification
    ): LibraryEntry? {
        val libraryEntry = libraryEntryModification.toLocalLibraryEntry().toLibraryEntry()

        return try {
            libraryEntriesService.updateLibraryEntry(
                libraryEntryModification.id,
                JSONAPIDocument(libraryEntry),
                filterForFullLibraryEntry.options
            ).get()
        } catch (e: HttpException) {
            if (e.code() == 404)
                throw NotFoundException(
                    "Library entry with ID '${libraryEntryModification.id}' does not exist.",
                    e
                )
            logE(
                "Received HTTP exception while updating library entry with ID '${libraryEntryModification.id}'.",
                e
            )
            null
        } catch (e: Exception) {
            logE("Failed to update library entry with ID '${libraryEntryModification.id}'.", e)
            null
        }
    }

    suspend fun deleteLibraryEntry(libraryEntryId: String): Boolean {
        return try {
            libraryEntriesService.deleteLibraryEntry(libraryEntryId).isSuccessful
        } catch (e: Exception) {
            logE("Failed to delete library entry with ID '$libraryEntryId'.", e)
            false
        }
    }

    suspend fun getFullLibraryEntry(libraryEntryId: String): LibraryEntry? {
        return try {
            libraryEntriesService.getLibraryEntry(
                libraryEntryId,
                filterForFullLibraryEntry.options
            ).get()
        } catch (e: Exception) {
            logE("Failed to get library entry with ID '$libraryEntryId'.", e)
            null
        }
    }

    suspend fun doesLibraryEntryExist(libraryEntryId: String): Boolean {
        return try {
            libraryEntriesService.getLibraryEntry(
                libraryEntryId,
                Filter().fields("libraryEntries", "id").options
            ).get() != null
        } catch (e: HttpException) {
            return e.code() == 404
        } catch (e: Exception) {
            logE("Failed to check for library entry with ID '${libraryEntryId}'.", e)
            return false
        }
    }

}