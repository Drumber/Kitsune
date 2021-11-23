package io.github.drumber.kitsune.ui.details.episodes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga
import io.github.drumber.kitsune.data.model.unit.MediaUnit
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.repository.MediaUnitRepository
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class EpisodesViewModel(private val mediaUnitRepository: MediaUnitRepository) : ViewModel() {

    private val resource = MutableLiveData<Resource>()

    fun setResource(resource: Resource) {
        if (resource != this.resource.value) {
            this.resource.value = resource

        }
    }

    val dataSource: Flow<PagingData<MediaUnit>> = resource.asFlow().flatMapLatest { resource ->
        val filter = Filter()
            .sort("number")
        val type = when(resource) {
            is Anime -> {
                filter.filter("media_id", resource.id)
                MediaUnitRepository.UnitType.Episode
            }
            is Manga -> {
                filter.filter("manga_id", resource.id)
                MediaUnitRepository.UnitType.Chapter
            }
        }
        mediaUnitRepository.episodesCollection(Kitsu.DEFAULT_PAGE_SIZE, filter, type)
    }.cachedIn(viewModelScope)

}