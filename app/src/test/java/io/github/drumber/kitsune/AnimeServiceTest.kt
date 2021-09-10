package io.github.drumber.kitsune

import io.github.drumber.kitsune.di.serviceModule
import io.github.drumber.kitsune.service.AnimeService
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

class AnimeServiceTest: AutoCloseKoinTest() {

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

}