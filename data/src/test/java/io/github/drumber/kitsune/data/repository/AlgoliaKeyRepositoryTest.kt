package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.AlgoliaKeyNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model.NetworkAlgoliaKey
import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model.NetworkAlgoliaKeyCollection
import io.github.drumber.kitsune.data.testutils.onSuspend
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class AlgoliaKeyRepositoryTest {

    private val faker = Faker()

    @Test
    fun shouldGetAllAlgoliaKeys(): Unit = runBlocking {
        // given
        val algoliaKeyCollection = NetworkAlgoliaKeyCollection(
            users = null,
            posts = null,
            media = NetworkAlgoliaKey(
                key = faker.internet().uuid(),
                index = "users"
            ),
            groups = null,
            characters = null
        )

        val remoteAlgoliaKeyDataSource = mock<AlgoliaKeyNetworkDataSource> {
            onSuspend { getAllAlgoliaKeys() } doReturn algoliaKeyCollection
        }

        val algoliaKeyRepository = AlgoliaKeyRepository(remoteAlgoliaKeyDataSource)

        // when
        val actualAlgoliaKeyCollection = algoliaKeyRepository.getAllAlgoliaKeys()

        // then
        verify(remoteAlgoliaKeyDataSource).getAllAlgoliaKeys()
        assertThat(actualAlgoliaKeyCollection)
            .usingRecursiveComparison()
            .isEqualTo(algoliaKeyCollection)
    }

    @Test
    fun shouldGetCachedAlgoliaKeys(): Unit = runBlocking {
        // given
        val algoliaKeyCollection = NetworkAlgoliaKeyCollection(
            users = null,
            posts = null,
            media = NetworkAlgoliaKey(
                key = faker.internet().uuid(),
                index = "users"
            ),
            groups = null,
            characters = null
        )

        val remoteAlgoliaKeyDataSource = mock<AlgoliaKeyNetworkDataSource> {
            onSuspend { getAllAlgoliaKeys() } doReturn algoliaKeyCollection
        }

        val algoliaKeyRepository = AlgoliaKeyRepository(remoteAlgoliaKeyDataSource)

        // when
        val algoliaKeyCollection1 = algoliaKeyRepository.getAllAlgoliaKeys()
        val algoliaKeyCollection2 = algoliaKeyRepository.getAllAlgoliaKeys()

        // then
        verify(remoteAlgoliaKeyDataSource, times(1)).getAllAlgoliaKeys()
        assertThat(algoliaKeyCollection1).isEqualTo(algoliaKeyCollection2)
    }
}