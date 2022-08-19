package io.github.drumber.kitsune.ui.details

import androidx.lifecycle.*
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryModification
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.data.model.user.Favorite
import io.github.drumber.kitsune.data.model.user.User
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.data.service.manga.MangaService
import io.github.drumber.kitsune.data.service.user.FavoriteService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.*
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
                _libraryEntry.value = it
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
                        libraryManager.mayRemoveSingleLibraryEntry(it)
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

    fun updateLibraryEntryStatus(status: Status) {
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
                    val modification = LibraryModification(existingLibraryEntryId, status = status)
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
                libraryManager.removeLibraryEntry(libraryEntry)
                _libraryEntry.postValue(null)
            } catch (e: Exception) {
                logE("Failed to remove library entry.", e)
                withContext(Dispatchers.Main) {
                    errorResponseListener?.invoke(ErrorResponseType.LibraryUpdateFailed)
                }
            }
        }
    }

    fun updateLibraryEntryRating(rating: Int?) {
        val mediaAdapter = mediaAdapter.value ?: return

        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // obtain library entry from database
                val entry = libraryEntryDao.getLibraryEntryFromMedia(mediaAdapter.id)
                    ?: libraryEntry.value // use fetched library entry if not cached in local database
                    ?: return@launch // ...or return

                val modification = LibraryModification(entry.id, ratingTwenty = updatedRating)

                val response = libraryManager.updateLibraryEntry(modification)
                if (response !is LibraryUpdateResponse.Error) {
                    // the library manager will store the updated library entry in the local database,
                    // so we need to get the new library entry from the database and show it to the UI
                    val localLibraryEntry =
                        libraryEntryDao.getLibraryEntryFromMedia(mediaAdapter.id)
                    _libraryEntry.postValue(localLibraryEntry)
                } else {
                    throw response.exception
                }
            } catch (e: Exception) {
                logE("Failed to update rating of library entry.", e)
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