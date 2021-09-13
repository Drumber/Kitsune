package io.github.drumber.kitsune

import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.manga.MangaService
import io.github.drumber.kitsune.di.serviceModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

class MangaServiceTest : AutoCloseKoinTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(serviceModule)
    }

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
        assertNull(singleManga?.createdAt)
        assertNotNull(singleManga?.titles)
        assertEquals("Monster", singleManga?.titles?.en)
    }

}