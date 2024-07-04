package io.github.drumber.kitsune.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.common.exception.ResourceUpdateFailed
import io.github.drumber.kitsune.data.presentation.model.mapping.Mapping
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.FavoriteRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.repository.MappingRepository
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavorite
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryWithModificationDao
import io.github.drumber.kitsune.domain_old.manager.library.LibraryManager
import io.github.drumber.kitsune.domain_old.manager.library.SynchronizationResult
import io.github.drumber.kitsune.domain_old.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain_old.mapper.toLibraryEntryModification
import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.library.LibraryEntriesService
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.AddNewLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.DeleteLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsViewModel(
    private val getLocalUserId: GetLocalUserIdUseCase,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val libraryEntriesService: LibraryEntriesService,
    private val libraryEntryWithModificationDao: LibraryEntryWithModificationDao,
    private val libraryManager: LibraryManager,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val mappingRepository: MappingRepository
) : ViewModel() {

    fun isLoggedIn() = isUserLoggedIn()

    private val _mediaModel = MutableLiveData<Media>()
    val mediaModel: LiveData<Media>
        get() = _mediaModel

    /** Combines local cached and fetched library entry. */
    private val _libraryEntryWrapper = MediatorLiveData<LibraryEntryWrapper?>()
    val libraryEntryWrapper: LiveData<LibraryEntryWrapper?>
        get() = _libraryEntryWrapper

    private val _favorite = MutableLiveData<Favorite?>()
    val favorite: LiveData<Favorite?>
        get() = _favorite

    private val _mappingsSate = MutableStateFlow<MediaMappingsSate>(MediaMappingsSate.Initial)
    val mappingsSate
        get() = _mappingsSate.asStateFlow()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val acceptInternalAction: (InternalAction) -> Unit

    val libraryChangeResultFlow: Flow<LibraryChangeResult>

    var areAllTileLanguagesShown = false

    init {
        val internalActionFlow = MutableSharedFlow<InternalAction>()
        libraryChangeResultFlow = internalActionFlow.mapNotNull { action ->
            when (action) {
                is InternalAction.LibraryUpdateResult -> LibraryUpdateResult(action.result)
                is InternalAction.AddNewLibraryEntryFailed -> AddNewLibraryEntryFailed
                is InternalAction.DeleteLibraryEntryFailed -> DeleteLibraryEntryFailed
            }
        }

        acceptInternalAction = { action ->
            viewModelScope.launch { internalActionFlow.emit(action) }
        }
    }

    fun initFromDeepLink(isAnime: Boolean, slug: String) {
        val filter = Filter()
            .filter("slug", slug)
            .fields("media", "id")

        viewModelScope.launch(Dispatchers.IO) {
            val media = if (isAnime) {
                animeRepository.getAllAnime(filter)
            } else {
                mangaRepository.getAllManga(filter)
            }

            if (media.isNullOrEmpty()) {
                logW("No media for slug '$slug' found.")
                return@launch
            }

            withContext(Dispatchers.Main) {
                initMediaModel(media.first())
            }
        }
    }

    fun initMediaModel(media: Media) {
        if (_mediaModel.value == null) {
            _mediaModel.value = media
            _isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                awaitAll(
                    async { loadFullMedia(media) },
                    async { loadLibraryEntry(media) },
                    async { loadFavorite(media) }
                )
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun loadFullMedia(media: Media) {
        val id = media.id
        val filter = Filter()
            .fields("categories", "slug", "title")

        val commonIncludes = arrayOf(
            "categories",
            "mediaRelationships",
            "mediaRelationships.destination"
        )

        try {
            val fullMedia = if (media is Anime) {
                filter.include(
                    *commonIncludes,
                    "animeProductions.producer",
                    "streamingLinks",
                    "streamingLinks.streamer"
                )
                animeRepository.getAnime(id, filter)
            } else {
                filter.include(*commonIncludes)
                mangaRepository.getManga(id, filter)
            } ?: throw NoDataException("Received data is null.")

            _mediaModel.postValue(fullMedia)
        } catch (e: Exception) {
            logE("Failed to load full media model.", e)
        }
    }

    private suspend fun loadLibraryEntry(media: Media) {
        val userId = getLocalUserId() ?: return

        // add local database as library entry source
        viewModelScope.launch(Dispatchers.Main) {
            _libraryEntryWrapper.addSource(libraryEntryWithModificationDao.getLibraryEntryWithModificationFromMediaAsLiveData(media.id)) {
                _libraryEntryWrapper.value = it?.let { entryWithModification ->
                    LibraryEntryWrapper(
                        entryWithModification.libraryEntry.toLibraryEntry(),
                        entryWithModification.libraryEntryModification?.toLibraryEntryModification(),
                        entryWithModification.libraryEntryModification?.state == SYNCHRONIZING
                    )
                }
            }
        }
        val filter = Filter()
            .filter("user_id", userId)
            .fields("libraryEntries", "status", "progress", "ratingTwenty")
            .pageLimit(1)

        if (media is Anime) {
            filter.filter("anime_id", media.id)
        } else {
            filter.filter("manga_id", media.id)
        }

        try {
            // fetch library entry from the server
            val libraryEntries = libraryEntriesService.allLibraryEntries(filter.options).get()
            if (!libraryEntries.isNullOrEmpty()) {
                // post fetched library entry that is possibly more up-to-date than the local cached one
                _libraryEntryWrapper.postValue(
                    LibraryEntryWrapper(libraryEntries[0], null)
                )
            } else if (libraryEntryWrapper.value != null) {
                // library entry is not available on the server but it is in the local cache, was it deleted?
                // -> local database cache is out of sync, remove entry from database
                libraryEntryWrapper.value?.let { libraryEntry ->
                    logD(
                        "There is no library entry on the server, but it exists in the local cache. " +
                                "Removed it from local database..."
                    )
                    withContext(Dispatchers.IO) {
                        _libraryEntryWrapper.postValue(null)
                        libraryEntry.libraryEntry.id?.let { libraryManager.mayRemoveLibraryEntryLocally(it) }
                    }
                }
            }
        } catch (e: Exception) {
            logE("Failed to load library entry.", e)
        }
    }

    private suspend fun loadFavorite(media: Media) {
        val userId = getLocalUserId() ?: return

        val filter = Filter()
            .filter("user_id", userId)
            .filter("item_id", media.id)
            .filter("item_type", if (media is Anime) "Anime" else "Manga")

        try {
            val favorites = favoriteRepository.getAllFavorites(filter)
            _favorite.postValue(favorites?.firstOrNull())
        } catch (e: Exception) {
            logE("Failed to load favorites.", e)
        }
    }

    fun loadMappingsIfNotAlreadyLoaded() {
        if (_mappingsSate.value != MediaMappingsSate.Initial) return
        val mediaModel = mediaModel.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _mappingsSate.value = MediaMappingsSate.Loading

            val mappingsState = try {
                val mappings = if (mediaModel is Anime) {
                    mappingRepository.getAnimeMappings(mediaModel.id)
                } else {
                    mappingRepository.getMangaMappings(mediaModel.id)
                } ?: emptyList()

                val mappingsWithKitsu = mappings + Mapping(
                    id = "",
                    externalSite = "kitsu/" + if (mediaModel is Anime) "anime" else "manga",
                    externalId = mediaModel.slug ?: mediaModel.id
                )

                MediaMappingsSate.Success(mappingsWithKitsu)
            } catch (e: Exception) {
                logE("Failed to load mappings.", e)
                MediaMappingsSate.Error(e.message ?: "Failed to load mappings.")
            }

            _mappingsSate.value = mappingsState
        }
    }

    fun updateLibraryEntryStatus(status: LibraryStatus) {
        val userId = getLocalUserId() ?: return
        val mediaModel = mediaModel.value ?: return
        val existingLibraryEntryId = libraryEntryWrapper.value?.libraryEntry?.id

        viewModelScope.launch(Dispatchers.IO) {
            if (existingLibraryEntryId.isNullOrBlank()) { // post new library entry
                try {
                    val newLibraryEntry = libraryManager.addNewLibraryEntry(
                        userId,
                        /*mediaModel*/ TODO(),
                        status
                    ) ?: throw Exception("Failed to post new library entry.")
                    _libraryEntryWrapper.postValue(
                        LibraryEntryWrapper(newLibraryEntry, null)
                    )
                } catch (e: Exception) {
                    logE("Failed to add new library entry.", e)
                    acceptInternalAction(InternalAction.AddNewLibraryEntryFailed)
                }
            } else { // update existing library entry
                val modification = LocalLibraryEntryModification
                    .withIdAndNulls(existingLibraryEntryId)
                    .copy(status = status)
                try {
                    val result = libraryManager.updateLibraryEntry(modification)
                    acceptInternalAction(InternalAction.LibraryUpdateResult(result))
                } catch (e: Exception) {
                    logE("Error while updating library entry.", e)
                }
            }
        }
    }

    fun removeLibraryEntry() {
        val libraryEntryId = libraryEntryWrapper.value?.libraryEntry?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = try {
                libraryManager.removeLibraryEntry(libraryEntryId)
            } catch (e: Exception) {
                logE("Failed to remove library entry.", e)
                false
            }
            if (isDeleted) {
                _libraryEntryWrapper.postValue(null)
            } else {
                acceptInternalAction(InternalAction.DeleteLibraryEntryFailed)
            }
        }
    }

    fun toggleFavorite() {
        val favorite = favorite.value

        viewModelScope.launch(Dispatchers.IO) {
            if (favorite == null) {
                val mediaItem = mediaModel.value ?: return@launch
                val userId = getLocalUserId() ?: return@launch

                // TODO: uncomment after migrating media items
                val newFavorite = NetworkFavorite(/*item = mediaItem,*/ user = NetworkUser(id = userId))
                try {
                    val resFavorite = favoriteRepository.createFavorite(newFavorite)
                    _favorite.postValue(resFavorite)
                } catch (e: Exception) {
                    logE("Failed to post favorite.", e)
                }
            } else {
                val favoriteId = favorite.id
                try {
                    val isSuccessful = favoriteRepository.deleteFavorite(favoriteId)
                    if (isSuccessful) {
                        _favorite.postValue(null)
                    } else {
                        throw ResourceUpdateFailed()
                    }
                } catch (e: Exception) {
                    logE("Failed to delete favorite.", e)
                }
            }
        }
    }
}

sealed class LibraryChangeResult {
    data class LibraryUpdateResult(val result: SynchronizationResult) : LibraryChangeResult()
    data object AddNewLibraryEntryFailed : LibraryChangeResult()
    data object DeleteLibraryEntryFailed : LibraryChangeResult()
}

private sealed class InternalAction {
    data class LibraryUpdateResult(val result: SynchronizationResult) : InternalAction()
    data object AddNewLibraryEntryFailed : InternalAction()
    data object DeleteLibraryEntryFailed : InternalAction()
}

sealed class MediaMappingsSate {
    data object Initial : MediaMappingsSate()
    data object Loading : MediaMappingsSate()
    data class Success(val mappings: List<Mapping>) : MediaMappingsSate()
    data class Error(val message: String) : MediaMappingsSate()
}
