package io.github.drumber.kitsune

import io.github.drumber.kitsune.domain.service.anime.EpisodesService
import io.github.drumber.kitsune.di.networkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class EpisodesServiceTest: BaseTest() {

    override val koinModules = listOf(networkModule)

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