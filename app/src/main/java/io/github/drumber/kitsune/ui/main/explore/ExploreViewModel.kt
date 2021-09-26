package io.github.drumber.kitsune.ui.main.explore

import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.ui.base.BaseCollectionViewModel
import kotlinx.coroutines.flow.Flow

class ExploreViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : BaseCollectionViewModel() {

    override fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>> {
        val filter = resourceSelector.filter
        val requestType = resourceSelector.requestType
        return when (resourceSelector.resourceType) {
            ResourceType.Anime -> animeRepository.animeCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter,
                requestType
            ) as Flow<PagingData<Resource>>

            ResourceType.Manga -> mangaRepository.mangaCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter,
                requestType
            ) as Flow<PagingData<Resource>>
        }
    }

    override fun getStoredResourceSelector(): ResourceSelector {
        return Defaults.DEFAULT_RESOURCE_SELECTOR
    }
}