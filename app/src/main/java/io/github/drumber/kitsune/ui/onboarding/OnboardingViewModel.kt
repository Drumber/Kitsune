package io.github.drumber.kitsune.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.media.Media
import io.github.drumber.kitsune.data.repository.media.AnimeRepository
import io.github.drumber.kitsune.data.repository.media.MangaRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.shared.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiSate = _uiState.asStateFlow()

    val localUser = userRepository.localUser

    init {
        loadPosterImages()
    }

    private fun loadPosterImages() {
        val baseFilter = Filter()
            .sort("-userCount")
            .fields("media", "posterImage")
            .pageLimit(10)

        val animeRandomOffset = (0..20).random()
        val mangaRandomOffset = (0..20).random()

        viewModelScope.launch(Dispatchers.IO) {
            val submitPosterImages = { posterImages: List<String> ->
                _uiState.update { it.copy(backgroundImages = it.backgroundImages + posterImages) }
            }

            launch {
                fetchPosterImages {
                    animeRepository.getAllAnime(baseFilter.copy().pageOffset(animeRandomOffset))
                }?.let { posterImages ->
                    submitPosterImages(posterImages)
                }
            }
            launch {
                fetchPosterImages {
                    mangaRepository.getAllManga(baseFilter.copy().pageOffset(mangaRandomOffset))
                }?.let { posterImages ->
                    submitPosterImages(posterImages)
                }
            }
        }
    }

    private suspend fun fetchPosterImages(request: suspend () -> List<Media>?): List<String>? {
        return try {
            request()?.mapNotNull { it.posterImage?.largeOrDown() }
        } catch (e: Exception) {
            logE("Failed to request poster images.", e)
            null
        }
    }
}