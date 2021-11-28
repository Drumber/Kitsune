package io.github.drumber.kitsune.ui.details.characters

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.production.Casting
import io.github.drumber.kitsune.data.repository.CastingRepository
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CharactersViewModel(
    private val castingRepository: CastingRepository,
    private val animeService: AnimeService
) : ViewModel() {

    private val filter = MutableLiveData<Filter>()

    private var mediaId: String? = null

    private val _languages = MutableLiveData<List<String>>()
    val languages: LiveData<List<String>>
        get() = _languages

    var selectedLanguage: String? = null
        private set

    fun setMediaId(id: String, isAnime: Boolean) {
        if (id == mediaId) return
        mediaId = id

        if (isAnime) {
            // fetch all languages of the anime
            viewModelScope.launch(Dispatchers.IO) {
                val langs = fetchLanguages(id) ?: emptyList()
                selectedLanguage = langs.find { it == "Japanese" }
                    ?: langs.find { it == "English" } ?: langs.firstOrNull()

                withContext(Dispatchers.Main) {
                    _languages.value = langs
                    updateFilter()
                }
            }
        } else {
            updateFilter()
        }
    }

    fun setLanguage(language: String) {
        if (languages.value?.contains(language) == true) {
            selectedLanguage = language
            updateFilter()
        }
    }

    private fun updateFilter() {
        val id = mediaId ?: return

        val filter = Filter()
            .filter("media_id", id)
            .filter("is_character", "true")
            .include("character", "person")
            .sort("-featured")
        selectedLanguage?.let { filter.filter("language", it) }

        this.filter.value = filter
    }

    private suspend fun fetchLanguages(id: String): List<String>? {
        return try {
            animeService.getLanguages(id)
        } catch (e: Exception) {
            logE("Failed to fetch languages for anime with id '$id'.", e)
            null
        }
    }

    val dataSource: Flow<PagingData<Casting>> = filter.asFlow().flatMapLatest { filter ->
        castingRepository.castingCollection(Kitsu.DEFAULT_PAGE_SIZE, filter)
    }.cachedIn(viewModelScope)

}