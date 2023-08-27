package io.github.drumber.kitsune.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.database.LibraryEntryDao
import io.github.drumber.kitsune.domain.manager.LibraryManager
import io.github.drumber.kitsune.domain.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.domain.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.user.Favorite
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.anime.AnimeService
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.domain.service.manga.MangaService
import io.github.drumber.kitsune.domain.service.user.FavoriteService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class DetailsViewModel(
    private val userRepository: UserRepository,
    private val libraryEntriesService: LibraryEntriesService,
    private val libraryEntryDao: LibraryEntryDao,
    private val libraryManager: LibraryManager,
    private val animeService: AnimeService,
    private val mangaService: MangaService,
    private val favoriteService: FavoriteService
) : ViewModel() {

    fun isLoggedIn() = userRepository.hasUser

    private val _mediaAdapter = MutableLiveData<MediaAdapter>()
    val mediaAdapter: LiveData<MediaAdapter>
        get() = _mediaAdapter

    /** Combines local cached and fetched library entry. */
    private val _libraryEntry = MediatorLiveData<LibraryEntry?>()
    val libraryEntry: LiveData<LibraryEntry?>
        get() = _libraryEntry

    private val _favorite = MutableLiveData<Favorite?>()
    val favorite: LiveData<Favorite?>
        get() = _favorite

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    var errorResponseListener: ((ErrorResponseType) -> Unit)? = null

    fun initFromDeepLink(isAnime: Boolean, slug: String) {
        val filter = Filter()
            .filter("slug", slug)
            .fields("media", "id")

        viewModelScope.launch(Dispatchers.IO) {
            val media = if (isAnime) {
                animeService.allAnime(filter.options).get()
            } else {
                mangaService.allManga(filter.options).get()
            }

            if (media.isNullOrEmpty()) {
                logW("No media for slug '$slug' found.")
                return@launch
            }

            val mediaAdapter = MediaAdapter.fromMedia(media.first())
            withContext(Dispatchers.Main) {
                initMediaAdapter(mediaAdapter)
            }
        }
    }

    fun initMediaAdapter(mediaAdapter: MediaAdapter) {
        if (_mediaAdapter.value == null) {
            _mediaAdapter.value = mediaAdapter
            _isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                awaitAll(
                    async { loadFullMedia(mediaAdapter) },
                    async { loadLibraryEntry(mediaAdapter) },
                    async { loadFavorite(mediaAdapter) }
                )
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun loadFullMedia(mediaAdapter: MediaAdapter) {
        val id = mediaAdapter.id
        val filter = Filter()
            .fields("categories", "slug", "title")

        val commonIncludes = arrayOf(
            "categories",
            "mediaRelationships",
            "mediaRelationships.destination"
        )

        try {
            val mediaModel = if (mediaAdapter.isAnime()) {
                filter.include(
                    *commonIncludes,
                    "animeProductions.producer",
                    "streamingLinks",
                    "streamingLinks.streamer"
                )
                animeService.getAnime(id, filter.options).get()
            } else {
                filter.include(*commonIncludes)
                mangaService.getManga(id, filter.options).get()
            } ?: throw ReceivedDataException("Received data is null.")

            val fullMediaAdapter = MediaAdapter.fromMedia(mediaModel)
            _mediaAdapter.postValue(fullMediaAdapter)
        } catch (e: Exception) {
            logE("Failed to load full media model.", e)
        }
    }

    private suspend fun loadLibraryEntry(mediaAdapter: MediaAdapter) {
        val userId = userRepository.user?.id ?: return

        // add local database as library entry source
        viewModelScope.launch(Dispatchers.Main) {
            _libraryEntry.addSource(libraryEntryDao.getLibraryEntryFromMediaLiveData(mediaAdapter.id)) {
                _libraryEntry.value = it?.toLibraryEntry()
            }
        }
        val filter = Filter()
            .filter("user_id", userId)
            .fields("libraryEntries", "status", "progress", "ratingTwenty")
            .pageLimit(1)

        if (mediaAdapter.isAnime()) {
            filter.filter("anime_id", mediaAdapter.id)
        } else {
            filter.filter("manga_id", mediaAdapter.id)
        }

        try {
            // fetch library entry from the server
            val libraryEntries = libraryEntriesService.allLibraryEntries(filter.options).get()
            if (!libraryEntries.isNullOrEmpty()) {
                // post fetched library entry that is possibly more up-to-date than the local cached one
                _libraryEntry.postValue(libraryEntries[0])
            } else if (libraryEntry.value != null) {
                // library entry is not available on the server but it is in the local cache, was it deleted?
                // -> local database cache is out of sync, remove entry from database
                libraryEntry.value?.let {
                    logD(
                        "There is no library entry on the server, but it exists in the local cache. " +
                                "Removed it from local database..."
                    )
                    withContext(Dispatchers.IO) {
                        _libraryEntry.postValue(null)
                        libraryManager.mayRemoveSingleLibraryEntry(it.toLocalLibraryEntry())
                    }
                }
            }
        } catch (e: Exception) {
            logE("Failed to load library entry.", e)
        }
    }

    private suspend fun loadFavorite(mediaAdapter: MediaAdapter) {
        val userId = userRepository.user?.id ?: return

        val filter = Filter()
            .filter("user_id", userId)
            .filter("item_id", mediaAdapter.id)
            .filter("item_type", if (mediaAdapter.isAnime()) "Anime" else "Manga")

        try {
            val favorites = favoriteService.allFavorites(filter.options).get()
            _favorite.postValue(favorites?.firstOrNull())
        } catch (e: Exception) {
            logE("Failed to load favorites.", e)
        }
    }

    fun updateLibraryEntryStatus(status: LibraryStatus) {
        val userId = userRepository.user?.id ?: return
        val mediaAdapter = mediaAdapter.value ?: return
        val existingLibraryEntryId = libraryEntry.value?.id

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (existingLibraryEntryId.isNullOrBlank()) { // post new library entry
                    val newLibraryEntry = libraryManager.postNewLibraryEntry(
                        userId,
                        mediaAdapter.media,
                        status
                    ) ?: throw Exception("Failed to post new library entry.")

                    _libraryEntry.postValue(newLibraryEntry)
                } else { // update existing library entry
                    val modification =
                        LocalLibraryEntryModification.withIdAndNulls(existingLibraryEntryId)
                            .copy(status = status)
                    val response = libraryManager.updateLibraryEntry(modification)

                    if (response is LibraryUpdateResponse.Error) {
                        throw response.exception
                    }
                }
            } catch (e: Exception) {
                logE("Failed to update library status.", e)
                withContext(Dispatchers.Main) {
                    errorResponseListener?.invoke(ErrorResponseType.LibraryUpdateFailed)
                }
            }
        }
    }

    fun removeLibraryEntry() {
        val libraryEntry = libraryEntry.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryManager.removeLibraryEntry(libraryEntry.toLocalLibraryEntry())
                _libraryEntry.postValue(null)
            } catch (e: Exception) {
                logE("Failed to remove library entry.", e)
                withContext(Dispatchers.Main) {
                    errorResponseListener?.invoke(ErrorResponseType.LibraryUpdateFailed)
                }
            }
        }
    }

    fun toggleFavorite() {
        val favorite = favorite.value

        viewModelScope.launch(Dispatchers.IO) {
            if (favorite == null) {
                val mediaItem = mediaAdapter.value?.media ?: return@launch
                val userId = userRepository.user?.id ?: return@launch

                val newFavorite = Favorite(item = mediaItem, user = User(id = userId))
                try {
                    val resFavorite =
                        favoriteService.postFavorite(JSONAPIDocument(newFavorite)).get()
                    _favorite.postValue(resFavorite)
                } catch (e: Exception) {
                    logE("Failed to post favorite.", e)
                }
            } else {
                val favoriteId = favorite.id ?: return@launch
                try {
                    val response = favoriteService.deleteFavorite(favoriteId).execute()
                    if (response.isSuccessful) {
                        _favorite.postValue(null)
                    } else {
                        throw HttpException(response)
                    }
                } catch (e: Exception) {
                    logE("Failed to delete favorite.", e)
                }
            }
        }
    }

    enum class ErrorResponseType {
        LibraryUpdateFailed
    }

}