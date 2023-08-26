package io.github.drumber.kitsune

import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.manga.MangaService
import io.github.drumber.kitsune.di.networkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MangaServiceTest : BaseTest() {

    override val koinModules = listOf(networkModule)

    @Test
    fun fetchAllManga() = runBlocking {
        val mangaService = getKoin().get<MangaService>()
        val response = mangaService.allManga()
        val mangaList = response.get()
        assertNotNull(mangaList)
        println("Received ${mangaList?.size} manga")
        println("First: ${mangaList?.first()}")
    }

    @Test
    fun fetchSingleManga() = runBlocking {
        val mangaService = getKoin().get<MangaService>()
        val response = mangaService.getManga("1")
        val manga = response.get()
        assertNotNull(manga)
        println("Received manga: $manga")
    }

    @Test
    fun fetchTrending() = runBlocking {
        val mangaService = getKoin().get<MangaService>()
        val response = mangaService.trending()
        val mangaList = response.get()
        assertNotNull(mangaList)
        println("Received ${mangaList?.size} manga")
        println("First: ${mangaList?.first()}")
    }

    @Test
    fun filterTest() = runBlocking {
        val mangaService = getKoin().get<MangaService>()
        val responseAll = mangaService.allManga(
            Filter()
                .pageOffset(5)
                .pageLimit(5)
                .options
        )
        assertEquals(5, responseAll.get()?.size)

        val responseSingle = mangaService.allManga(
            Filter()
                .filter("slug", "monster")
                .fields("manga", "titles")
                .options
        )
        val singleManga = responseSingle.get()?.first()
        assertNotNull(singleManga?.titles)
        assertEquals("Monster", singleManga?.titles?.en)
    }

    @Test
    fun filterIncludeTest() = runBlocking {
        val mangaService = getKoin().get<MangaService>()

        val response = mangaService.allManga(
            Filter()
                .filter("slug", "one-piece")
                .include(
                    "categories",
                    "mediaRelationships",
                    "mediaRelationships.destination"
                )
                .options
        )

        val manga = response.get()?.first()
        assertNotNull(manga)
        println("Manga including categories: $manga")
        println("\nGot ${manga?.mediaRelationships?.size} related media objects.")

        assertFalse(manga?.categories.isNullOrEmpty())
        assertFalse(manga?.mediaRelationships.isNullOrEmpty())
        assertNotNull(manga?.mediaRelationships?.firstOrNull())
    }

}