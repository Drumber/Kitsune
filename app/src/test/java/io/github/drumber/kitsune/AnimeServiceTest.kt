package io.github.drumber.kitsune

import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.di.serviceModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

class AnimeServiceTest : AutoCloseKoinTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(serviceModule)
    }

    @Test
    fun fetchAllAnime() = runBlocking {
        val animeService = getKoin().get<AnimeService>()
        val response = animeService.allAnime()
        val animeList = response.get()
        assertNotNull(animeList)
        println("Received ${animeList?.size} anime")
        println("First: ${animeList?.first()}")
    }

    @Test
    fun fetchSingleAnime() = runBlocking {
        val animeService = getKoin().get<AnimeService>()
        val response = animeService.getAnime("1")
        val anime = response.get()
        assertNotNull(anime)
        println("Received anime: $anime")
    }

    @Test
    fun fetchTrending() = runBlocking {
        val animeService = getKoin().get<AnimeService>()
        val response = animeService.trending()
        val animeList = response.get()
        assertNotNull(animeList)
        println("Received ${animeList?.size} anime")
        println("First: ${animeList?.first()}")
    }

    @Test
    fun filterTest() = runBlocking {
        val animeService = getKoin().get<AnimeService>()
        val responseAll = animeService.allAnime(
            Filter()
                .pageOffset(5)
                .pageLimit(5)
                .options
        )
        assertEquals(5, responseAll.get()?.size)

        val responseSingle = animeService.allAnime(
            Filter()
                .filter("slug", "cowboy-bebop")
                .fields("anime", "titles")
                .include("categories")
                .options
        )
        val singleAnime = responseSingle.get()?.first()
        assertNull(singleAnime?.createdAt)
        assertNotNull(singleAnime?.titles)
        assertEquals("Cowboy Bebop", singleAnime?.titles?.en)
    }

    @Test
    fun filterIncludeTest() = runBlocking {
        val animeService = getKoin().get<AnimeService>()

        val response = animeService.allAnime(
            Filter()
                .filter("slug", "one-piece")
                .include("categories", "animeProductions.producer")
                .options
        )
        val anime = response.get()?.first()
        println("Anime with included relationships: $anime")
        assertNotNull(anime)
        assertNotNull(anime?.categories)
        assertFalse(anime?.categories.isNullOrEmpty())
        assertNotNull(anime?.animeProduction)
        assertFalse(anime?.animeProduction.isNullOrEmpty())
    }

}