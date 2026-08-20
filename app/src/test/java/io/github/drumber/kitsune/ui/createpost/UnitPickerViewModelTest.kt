package io.github.drumber.kitsune.ui.createpost

import androidx.paging.PagingData
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.data.repository.MediaUnitRepository
import io.github.drumber.kitsune.data.repository.MediaUnitRepository.MediaUnitType
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class UnitPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mediaUnitRepository(): MediaUnitRepository = mock {
        on { mediaUnitPager(any(), any(), any()) } doReturn flowOf(PagingData.empty<MediaUnit>())
    }

    @Test
    fun `unitPager requests an episode pager for anime`() {
        val repository = mediaUnitRepository()
        val vm = UnitPickerViewModel(repository)

        vm.unitPager("media-1", isAnime = true)

        verify(repository).mediaUnitPager(
            eq(MediaUnitType.EPISODE),
            any(),
            eq(Kitsu.DEFAULT_PAGE_SIZE)
        )
    }

    @Test
    fun `unitPager requests a chapter pager for manga`() {
        val repository = mediaUnitRepository()
        val vm = UnitPickerViewModel(repository)

        vm.unitPager("media-1", isAnime = false)

        verify(repository).mediaUnitPager(
            eq(MediaUnitType.CHAPTER),
            any(),
            eq(Kitsu.DEFAULT_PAGE_SIZE)
        )
    }

    @Test
    fun `unitPager caches the pager and reuses it on subsequent calls`() {
        val repository = mediaUnitRepository()
        val vm = UnitPickerViewModel(repository)

        val first = vm.unitPager("media-1", isAnime = true)
        val second = vm.unitPager("media-2", isAnime = false)

        assertThat(second).isSameAs(first)
        verify(repository, times(1)).mediaUnitPager(any(), any(), any())
    }
}
