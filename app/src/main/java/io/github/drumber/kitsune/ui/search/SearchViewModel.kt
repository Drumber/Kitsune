package io.github.drumber.kitsune.ui.search

import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.ui.base.BaseCollectionViewModel
import kotlinx.coroutines.flow.Flow

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : BaseCollectionViewModel() {

    override fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>> {
        val filter = resourceSelector.filter
        return when (resourceSelector.resourceType) {
            ResourceType.Anime -> animeRepository.animeCollection(Kitsu.DEFAULT_PAGE_SIZE, filter) as Flow<PagingData<Resource>>
            ResourceType.Manga -> mangaRepository.mangaCollection(Kitsu.DEFAULT_PAGE_SIZE, filter) as Flow<PagingData<Resource>>
        }
    }

}