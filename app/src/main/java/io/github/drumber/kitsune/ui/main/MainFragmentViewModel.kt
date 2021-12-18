package io.github.drumber.kitsune.ui.main

import androidx.lifecycle.*
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.model.MediaType
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.data.service.manga.MangaService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.Dispatchers

class MainFragmentViewModel(
    private val animeService: AnimeService,
    private val mangaService: MangaService
) : ViewModel() {

    private val animeReload = MutableLiveData(false)
    private val mangaReload = MutableLiveData(false)

    private val animeExploreSections = mutableMapOf(
        // trending
        TRENDING to createLiveData(animeReload) {
            animeService.trending(Filter().limit(10).options).get()
        },
        // top airing
        TOP_AIRING to createLiveData(animeReload) {
            animeService.allAnime(FILTER_TOP_AIRING.options).get()
        },
        // top upcoming
        TOP_UPCOMING to createLiveData(animeReload) {
            animeService.allAnime(FILTER_TOP_UPCOMING.options).get()
        },
        // highest rated
        HIGHEST_RATED to createLiveData(animeReload) {
            animeService.allAnime(FILTER_HIGHEST_RATED.options).get()
        },
        // most popular
        MOST_POPULAR to createLiveData(animeReload) {
            animeService.allAnime(FILTER_MOST_POPULAR.options).get()
        }
    )

    private val mangaExploreSections = mutableMapOf(
        // trending
        TRENDING to createLiveData(mangaReload) {
            mangaService.trending(Filter().limit(10).options).get()
        },
        // top airing
        TOP_AIRING to createLiveData(mangaReload) {
            mangaService.allManga(FILTER_TOP_AIRING.options).get()
        },
        // top upcoming
        TOP_UPCOMING to createLiveData(mangaReload) {
            mangaService.allManga(FILTER_TOP_UPCOMING.options).get()
        },
        // highest rated
        HIGHEST_RATED to createLiveData(mangaReload) {
            mangaService.allManga(FILTER_HIGHEST_RATED.options).get()
        },
        // most popular
        MOST_POPULAR to createLiveData(mangaReload) {
            mangaService.allManga(FILTER_MOST_POPULAR.options).get()
        }
    )

    fun getAnimeExploreLiveData(type: String) = animeExploreSections[type]
        ?: throw IllegalArgumentException("There is no anime live data for type ''$type.")

    fun getMangaExploreLiveData(type: String) = mangaExploreSections[type]
        ?: throw IllegalArgumentException("There is no manga live data for type ''$type.")

    private fun <T> createLiveData(reloadLiveData: LiveData<*>, call: suspend () -> List<T>?) =
        Transformations.switchMap(reloadLiveData) {
            liveData(Dispatchers.IO) {
                val responseData = processCall(call)
                emit(responseData)
            }
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
        const val TRENDING = "anime_trending"
        const val TOP_AIRING = "top_airing"
        const val TOP_UPCOMING = "top_upcoming"
        const val HIGHEST_RATED = "highest_rated"
        const val MOST_POPULAR = "most_popular"

        val FILTER_TOP_AIRING = createFilter("current")
        val FILTER_TOP_UPCOMING = createFilter("upcoming")
        val FILTER_HIGHEST_RATED = createFilter(sortBy = SortFilter.AVERAGE_RATING_DESC)
        val FILTER_MOST_POPULAR = createFilter(sortBy = SortFilter.POPULARITY_DESC)

        private fun createFilter(
            filterType: String? = null,
            sortBy: SortFilter = SortFilter.POPULARITY_DESC,
            type: MediaType = MediaType.Anime
        ) = Filter().apply {
            pageLimit(10)
            filterType?.let { filter("status", it) }
            sort(sortBy.queryParam)
            fields(type.type, *Defaults.MINIMUM_COLLECTION_FIELDS)
        }
    }

}