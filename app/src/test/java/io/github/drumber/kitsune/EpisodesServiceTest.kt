package io.github.drumber.kitsune

import io.github.drumber.kitsune.data.service.EpisodesService
import io.github.drumber.kitsune.di.serviceModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
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
    fun fetchAllEpisodes() = runBlocking {
        val episodesService = getKoin().get<EpisodesService>()
        val response = episodesService.allEpisodes()
        val episodeList = response.get()
        assertNotNull(episodeList)
        println("Received ${episodeList?.size} episodes")
        println("First: ${episodeList?.first()}")
    }

    @Test
    fun fetchSingleEpisode() = runBlocking {
        val episodesService = getKoin().get<EpisodesService>()
        val response = episodesService.getEpisode("28")
        val episode = response.get()
        assertNotNull(episode)
        println("Received episode: $episode")
    }

}