package io.github.drumber.kitsune.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.constants.SortFilter.desc
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.ResponseData
import io.github.drumber.kitsune.util.logE

class MainFragmentViewModel(
    private val animeService: AnimeService
) : ViewModel() {

    val trending: LiveData<ResponseData<List<Anime>>> = liveData {
        val responseData = processCall {
            animeService.trending(Filter()
                .limit(10)
                .options).get()
        }
        emit(responseData)
    }

    val topAiring: LiveData<ResponseData<List<Anime>>> = liveData {
        val responseData = processCall { animeService.allAnime(FILTER_TOP_AIRING.options).get() }
        emit(responseData)
    }

    val topUpcoming: LiveData<ResponseData<List<Anime>>> = liveData {
        val responseData = processCall { animeService.allAnime(FILTER_TOP_UPCOMING.options).get() }
        emit(responseData)
    }

    val highestRated: LiveData<ResponseData<List<Anime>>> = liveData {
        val responseData = processCall { animeService.allAnime(FILTER_HIGHEST_RATED.options).get() }
        emit(responseData)
    }

    val mostPopular: LiveData<ResponseData<List<Anime>>> = liveData {
        val responseData = processCall { animeService.allAnime(FILTER_MOST_POPULAR.options).get() }
        emit(responseData)
    }

    private suspend fun <T> processCall(call: suspend () -> List<T>?): ResponseData<List<T>> {
        return try {
            val data = call() ?: throw ReceivedDataException("Received data is 'null'.")
            ResponseData.Success(data)
        } catch (e: Exception) {
            logE("Failed to load data.", e)
            ResponseData.Error(e)
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