package io.github.drumber.kitsune.ui.details.characters

import io.github.drumber.kitsune.data.repository.CharacterRepository
import io.github.drumber.kitsune.data.repository.FavoriteRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.character
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()

    private fun viewModel(
        characterRepository: CharacterRepository = mock(),
        getLocalUserId: GetLocalUserIdUseCase = mock { on { invoke() } doReturn "user-1" },
        favoriteRepository: FavoriteRepository = mock()
    ) = CharacterDetailsViewModel(characterRepository, getLocalUserId, favoriteRepository)

    @Test
    fun `initial ui state has no loading and no media characters`() {
        val vm = viewModel()

        assertThat(vm.uiState.value.isLoadingMediaCharacters).isFalse()
        assertThat(vm.uiState.value.hasMediaCharacters).isFalse()
    }

    @Test
    fun `initCharacter emits the provided character`() = runTest {
        val character = character(faker).copy(id = "char-1")
        val vm = viewModel()

        vm.initCharacter(character)

        assertThat(vm.characterFlow.first()).isEqualTo(character)
    }

    @Test
    fun `toggleFavorite returns false and does nothing when no character is loaded`() {
        val favoriteRepository = mock<FavoriteRepository>()
        val vm = viewModel(favoriteRepository = favoriteRepository)

        assertThat(vm.toggleFavorite()).isFalse()
        verifyNoInteractions(favoriteRepository)
    }

    @Test
    fun `toggleFavorite returns false when there is no local user`() = runTest {
        val character = character(faker).copy(id = "char-1")
        val vm = viewModel(getLocalUserId = mock { on { invoke() } doReturn null })
        vm.initCharacter(character)

        assertThat(vm.toggleFavorite()).isFalse()
    }
}
