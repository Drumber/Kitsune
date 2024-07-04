package io.github.drumber.kitsune.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.presentation.model.media.MediaType
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logV
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.set

class MainFragmentViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val animeReload = MutableLiveData(Any())
    private val mangaReload = MutableLiveData(Any())

    // Contains a boolean value for each explore section type which represents
    // whether the entry is reloading from network or not.
    private var animeReloadMap = mutableMapOf<String, Boolean>()
    private var mangaReloadMap = mutableMapOf<String, Boolean>()

    var reloadFinishedListener: (() -> Unit)? = null

    private val animeExploreSections = mutableMapOf(
        // trending
        createAnimeExploreEntry(TRENDING) {
            animeRepository.getTrending(Filter().limit(10))
        },
        // top airing
        createAnimeExploreEntry(TOP_AIRING) {
            animeRepository.getAllAnime(FILTER_TOP_AIRING)
        },
        // top upcoming
        createAnimeExploreEntry(TOP_UPCOMING) {
            animeRepository.getAllAnime(FILTER_TOP_UPCOMING)
        },
        // highest rated
        createAnimeExploreEntry(HIGHEST_RATED) {
            animeRepository.getAllAnime(FILTER_HIGHEST_RATED)
        },
        // most popular
        createAnimeExploreEntry(MOST_POPULAR) {
            animeRepository.getAllAnime(FILTER_MOST_POPULAR)
        }
    )

    private val mangaExploreSections = mutableMapOf(
        // trending
        createMangaExploreEntry(TRENDING) {
            mangaRepository.getTrending(Filter().limit(10))
        },
        // top airing
        createMangaExploreEntry(TOP_AIRING) {
            mangaRepository.getAllManga(FILTER_TOP_AIRING)
        },
        // top upcoming
        createMangaExploreEntry(TOP_UPCOMING) {
            mangaRepository.getAllManga(FILTER_TOP_UPCOMING)
        },
        // highest rated
        createMangaExploreEntry(HIGHEST_RATED) {
            mangaRepository.getAllManga(FILTER_HIGHEST_RATED)
        },
        // most popular
        createMangaExploreEntry(MOST_POPULAR) {
            mangaRepository.getAllManga(FILTER_MOST_POPULAR)
        }
    )

    fun getAnimeExploreLiveData(type: String) = animeExploreSections[type]
        ?: throw IllegalArgumentException("There is no anime live data for type ''$type.")

    fun getMangaExploreLiveData(type: String) = mangaExploreSections[type]
        ?: throw IllegalArgumentException("There is no manga live data for type ''$type.")

    fun refreshAnimeData() {
        if (!animeReloadMap.containsValue(true)) {
            animeReload.postValue(Any())
        }
    }

    fun refreshMangaData() {
        if (!mangaReloadMap.containsValue(true)) {
            mangaReload.postValue(Any())
        }
    }

    private fun <T> createAnimeExploreEntry(key: String, call: suspend () -> List<T>?) = Pair(
        key,
        animeReload.switchMap {
            liveData(Dispatchers.IO) {
                animeReloadMap[key] = true
                val responseData = processCall(call)
                emit(responseData)
                animeReloadMap[key] = false
                onEntryReloadFinished()
            }
        }
    ).also { animeReloadMap[key] = false }

    private fun <T> createMangaExploreEntry(key: String, call: suspend () -> List<T>?) = Pair(
        key,
        mangaReload.switchMap {
            liveData(Dispatchers.IO) {
                mangaReloadMap[key] = true
                val responseData = processCall(call)
                mangaReloadMap[key] = false
                onEntryReloadFinished()
                emit(responseData)
            }
        }
    ).also { mangaReloadMap[key] = false }

    private fun onEntryReloadFinished() {
        logV("isSomeEntryReloading: ${isSomeEntryReloading()} " +
                "Remaining: " +
                "Anime: " + animeReloadMap.count { it.value } +
                " Manga: " + mangaReloadMap.count { it.value }
        )
        if (!isSomeEntryReloading()) {
            viewModelScope.launch(Dispatchers.Main) {
                reloadFinishedListener?.invoke()
            }
        }
    }

    fun isSomeEntryReloading(): Boolean {
        return animeReloadMap.containsValue(true) || mangaReloadMap.containsValue(true)
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