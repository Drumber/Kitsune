package io.github.drumber.kitsune

import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.production.CastingService
import io.github.drumber.kitsune.di.networkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class CastingServiceTest : BaseTest() {

    override val koinModules = listOf(networkModule)

    @Test
    fun fetchCastings() = runBlocking {
        val castingService = getKoin().get<CastingService>()
        val response = castingService.allCastings(Filter()
            .filter("media_id", "12")
            .filter("media_type", "Anime")
            .filter("is_character", "true")
            .filter("language", "Japanese")
            .include("character")
            .sort("-featured")
            .pageLimit(10)
            .options
        )
        val castingList = response.get()
        assertNotNull(castingList)
        println("Received ${castingList?.size} castings")
        println("First: ${castingList?.first()}")
        assertNotNull(castingList?.first()?.character)
    }

}