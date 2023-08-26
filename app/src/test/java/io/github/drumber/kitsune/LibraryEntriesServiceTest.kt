package io.github.drumber.kitsune

import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.di.networkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class LibraryEntriesServiceTest : BaseTest() {

    override val koinModules = listOf(networkModule)

    private val defaultFilter = Filter()
        .filter("user_id", "1")
        .include("anime", "manga")
        .fields("anime", "slug", "canonicalTitle")
        .fields("manga", "slug", "canonicalTitle")

    @Test
    fun fetchAllLibraryEntries() = runBlocking {
        val libraryService = getKoin().get<LibraryEntriesService>()
        val response = libraryService.allLibraryEntries(defaultFilter.options)
        val libraryList = response.get()
        assertNotNull(libraryList)
        println("Received ${libraryList?.size} library entries")
        println("First: ${libraryList?.first()}")
        println("First included anime: ${libraryList?.firstOrNull()?.anime}")
        println("First included manga: ${libraryList?.firstOrNull()?.manga}")
        // either anime or manga must not be null
        assertNotNull(libraryList?.firstOrNull()?.let { it.anime ?: it.manga })
    }

}