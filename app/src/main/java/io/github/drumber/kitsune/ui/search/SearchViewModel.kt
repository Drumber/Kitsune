package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.repository.SearchRepository
import io.github.drumber.kitsune.ui.base.BaseCollectionViewModel
import io.github.drumber.kitsune.util.SearchResourceProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    algoliaKeyRepository: AlgoliaKeyRepository,
    private val objectMapper: ObjectMapper
) : BaseCollectionViewModel() {

    val searchHandler = SearchHandler(algoliaKeyRepository, viewModelScope)

    var isCurrentlySearching: Boolean = false
        set(value) {
            field = value
            // trigger data invalidation; getData() will be called
            _resourceSelector.value = _resourceSelector.value
        }

    override fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>> {
        val filter = resourceSelector.filter
        return if(!isCurrentlySearching) {
            when (resourceSelector.resourceType) {
                ResourceType.Anime -> animeRepository.animeCollection(Kitsu.DEFAULT_PAGE_SIZE, filter) as Flow<PagingData<Resource>>
                ResourceType.Manga -> mangaRepository.mangaCollection(Kitsu.DEFAULT_PAGE_SIZE, filter) as Flow<PagingData<Resource>>
            }
        } else {
            val searcher = searchHandler.searcher
            val type =  when (resourceSelector.resourceType) {
                ResourceType.Anime -> Anime::class.java
                ResourceType.Manga -> Manga::class.java
            }
            SearchRepository.search(Kitsu.DEFAULT_PAGE_SIZE, filter, searcher) { hit ->
                val json = SearchResourceProcessor.processSearchResource(hit.json)
                val jsonString = Json{ prettyPrint = true }.encodeToString(json)
                objectMapper.readValue(jsonString, type)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchHandler.cancel()
    }

}