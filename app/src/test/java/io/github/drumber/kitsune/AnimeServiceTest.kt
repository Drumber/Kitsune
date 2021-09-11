package io.github.drumber.kitsune

import io.github.drumber.kitsune.api.Filter
import io.github.drumber.kitsune.api.service.AnimeService
import io.github.drumber.kitsune.di.serviceModule
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
    fun fetchAllAnime() {
        val animeService = getKoin().get<AnimeService>()
        val response = animeService.allAnime().execute()
        assertTrue(response.isSuccessful)
        val animeList = response.body()?.get()
        assertNotNull(animeList)
        println("Received ${animeList?.size} anime")
        println("First: ${animeList?.first()}")
    }

    @Test
    fun fetchSingleAnime() {
        val animeService = getKoin().get<AnimeService>()
        val response = animeService.getAnime("1").execute()
        assertTrue(response.isSuccessful)
        val anime = response.body()?.get()
        assertNotNull(anime)
        println("Received anime: $anime")
    }

    @Test
    fun fetchTrending() {
        val animeService = getKoin().get<AnimeService>()
        val response = animeService.trending().execute()
        assertTrue(response.isSuccessful)
        val animeList = response.body()?.get()
        assertNotNull(animeList)
        println("Received ${animeList?.size} anime")
        println("First: ${animeList?.first()}")
    }

    @Test
    fun filterTest() {
        val animeService = getKoin().get<AnimeService>()
        val responseAll = animeService.allAnime(
            Filter()
                .pageOffset(5)
                .pageLimit(5)
                .options
        ).execute()
        assertTrue(responseAll.isSuccessful)
        assertEquals(5, responseAll.body()?.get()?.size)

        val responseSingle = animeService.allAnime(
            Filter()
                .filter("slug", "cowboy-bebop")
                .fields("anime", "titles")
                .options
        ).execute()
        assertTrue(responseSingle.isSuccessful)
        val singleAnime = responseSingle.body()?.get()?.first()
        assertNull(singleAnime?.createdAt)
        assertNotNull(singleAnime?.titles)
        assertEquals("Cowboy Bebop", singleAnime?.titles?.en)
    }

}