package io.github.drumber.kitsune.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.constants.SortFilter.desc
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class MainFragmentViewModel(
    private val animeService: AnimeService
) : ViewModel() {

    val trending: LiveData<List<Anime>> = liveData {
        val anime = animeService.trending(Filter()
            .limit(10)
            .options).get()
        if(anime != null) {
            emit(anime)
        }
    }

    val topAiring: LiveData<List<Anime>> = liveData {
        val anime = animeService.allAnime(FILTER_TOP_AIRING.options).get()
        if(anime != null) {
            emit(anime)
        }
    }

    companion object {
        val FILTER_TOP_AIRING = Filter()
            .pageLimit(10)
            .filter("status", "current")
            .sort(SortFilter.POPULARITY.desc())
    }

}