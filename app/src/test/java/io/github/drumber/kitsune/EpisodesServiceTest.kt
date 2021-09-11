package io.github.drumber.kitsune

import io.github.drumber.kitsune.api.service.EpisodesService
import io.github.drumber.kitsune.di.serviceModule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

class EpisodesServiceTest: AutoCloseKoinTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(serviceModule)
    }

    @Test
    fun fetchAllEpisodes() {
        val episodesService = getKoin().get<EpisodesService>()
        val response = episodesService.allEpisodes().execute()
        assertTrue(response.isSuccessful)
        val episodeList = response.body()?.get()
        assertNotNull(episodeList)
        println("Received ${episodeList?.size} episodes")
        println("First: ${episodeList?.first()}")
    }

    @Test
    fun fetchSingleEpisode() {
        val episodesService = getKoin().get<EpisodesService>()
        val response = episodesService.getEpisode("28").execute()
        assertTrue(response.isSuccessful)
        val episode = response.body()?.get()
        assertNotNull(episode)
        println("Received episode: $episode")
    }

}