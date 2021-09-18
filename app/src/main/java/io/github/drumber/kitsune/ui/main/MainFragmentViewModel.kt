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

    val topUpcoming: LiveData<List<Anime>> = liveData {
        val anime = animeService.allAnime(FILTER_TOP_UPCOMING.options).get()
        if(anime != null) {
            emit(anime)
        }
    }

    val highestRated: LiveData<List<Anime>> = liveData {
        val anime = animeService.allAnime(FILTER_HIGHEST_RATED.options).get()
        if(anime != null) {
            emit(anime)
        }
    }

    val mostPopular: LiveData<List<Anime>> = liveData {
        val anime = animeService.allAnime(FILTER_MOST_POPULAR.options).get()
        if(anime != null) {
            emit(anime)
        }
    }

    companion object {
        val FILTER_TOP_AIRING = createFilter("current")
        val FILTER_TOP_UPCOMING = createFilter("upcoming")
        val FILTER_HIGHEST_RATED = Filter()
            .pageLimit(10)
            .sort(SortFilter.AVERAGE_RATING.desc())
        val FILTER_MOST_POPULAR = Filter()
            .pageLimit(10)
            .sort(SortFilter.POPULARITY.desc())


        private inline fun createFilter(filterType: String) = Filter()
            .pageLimit(10)
            .filter("status", filterType)
            .sort(SortFilter.POPULARITY.desc())
    }

}