package io.github.drumber.kitsune

import io.github.drumber.kitsune.data.service.manga.ChaptersService
import io.github.drumber.kitsune.di.serviceModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

class ChaptersServiceTest: AutoCloseKoinTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(serviceModule)
    }

    @Test
    fun fetchAllChapters() = runBlocking {
        val chaptersService = getKoin().get<ChaptersService>()
        val response = chaptersService.allChapters()
        val chapterList = response.get()
        assertNotNull(chapterList)
        println("Received ${chapterList?.size} chapters")
        println("First: ${chapterList?.first()}")
    }

    @Test
    fun fetchSingleChapter() = runBlocking {
        val chapterService = getKoin().get<ChaptersService>()
        val response = chapterService.getChapter("403830")
        val chapter = response.get()
        assertNotNull(chapter)
        println("Received chapter: $chapter")
    }

}