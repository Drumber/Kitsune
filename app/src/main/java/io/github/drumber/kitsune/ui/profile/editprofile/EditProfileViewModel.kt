package io.github.drumber.kitsune.ui.profile.editprofile

import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.search.dsl.attributesToHighlight
import com.algolia.search.dsl.attributesToRetrieve
import com.algolia.search.dsl.query
import com.algolia.search.dsl.responseFields
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.Query
import com.algolia.search.model.search.RemoveStopWords
import com.algolia.search.model.search.RemoveWordIfNoResults
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.manager.SearchProvider
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.SearchType
import io.github.drumber.kitsune.domain.model.infrastructure.production.Character
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.model.infrastructure.user.UserImageUpload
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.domain.model.ui.media.originalOrDown
import io.github.drumber.kitsune.domain.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.user.ProfileLinkService
import io.github.drumber.kitsune.domain.service.user.UserImageUploadService
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

class EditProfileViewModel(
    private val userRepository: UserRepository,
    algoliaKeyRepository: AlgoliaKeyRepository,
    private val service: UserService,
    private val imageUploadService: UserImageUploadService,
    private val profileLinkService: ProfileLinkService
) : ViewModel() {

    val loadingStateFlow: StateFlow<LoadingState>

    val profileStateFlow: StateFlow<ProfileState>
    val profileImageStateFlow: StateFlow<ProfileImageState>
    val profileLinkEntriesFlow: StateFlow<List<ProfileLinkEntry>>
    val canUpdateProfileFlow: Flow<Boolean>

    private val initialProfileLinksFlow = MutableSharedFlow<List<ProfileLinkEntry>>(1)
    private val _profileLinkSitesFlow = MutableSharedFlow<List<ProfileLinkSite>>(1)
    private val _profileLinkSitesLoadStateFlow = MutableStateFlow(false)

    val profileLinkSitesFlow
        get() = _profileLinkSitesFlow.asSharedFlow()

    val profileLinkSitesLoadStateFlow: StateFlow<Boolean>
        get() = _profileLinkSitesLoadStateFlow.asStateFlow()

    val profileState
        get() = profileStateFlow.value

    val profileImageState
        get() = profileImageStateFlow.value

    val profileLinkEntries
        get() = profileLinkEntriesFlow.value

    val profileLinkSites
        get() = _profileLinkSitesFlow.replayCache.firstOrNull()

    val acceptProfileChanges: (ProfileState) -> Unit
    val acceptProfileImageChanges: (ProfileImageState) -> Unit
    val acceptProfileLinkAction: (ProfileLinkAction) -> Unit

    private val acceptLoadingState: (LoadingState) -> Unit

    private val searchProvider: SearchProvider
    private val connectionHandler = ConnectionHandler()
    private val _searchBoxConnectorFlow = MutableSharedFlow<SearchBoxConnector<ResponseSearch>>(1)
    val searchBoxConnectorFlow
        get() = _searchBoxConnectorFlow.asSharedFlow()

    var currentImagePickerType: ImagePickerType? = null

    init {
        val user = userRepository.user

        val initialProfileState = ProfileState(
            location = user?.location ?: "",
            birthday = user?.birthday ?: "",
            gender = user?.getGenderWithoutCustomGender() ?: "",
            customGender = user?.getCustomGenderOrNull() ?: "",
            waifuOrHusbando = user?.waifuOrHusbando ?: "",
            character = user?.waifu,
            about = user?.about ?: ""
        )

        val initialProfileImageState = ProfileImageState(
            currentAvatarUrl = user?.avatar?.originalOrDown(),
            currentCoverUrl = user?.coverImage?.originalOrDown()
        )

        val _profileStateFlow = MutableSharedFlow<ProfileState>()
        profileStateFlow = _profileStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = initialProfileState
            )

        val _profileImageStateFlow = MutableSharedFlow<ProfileImageState>()
        profileImageStateFlow = _profileImageStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = initialProfileImageState
            )

        val _profileLinkEntriesStateFlow = MutableSharedFlow<List<ProfileLinkEntry>>()
        profileLinkEntriesFlow = _profileLinkEntriesStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList()
            )

        acceptProfileChanges = { changes ->
            viewModelScope.launch { _profileStateFlow.emit(changes) }
        }

        acceptProfileImageChanges = { changes ->
            viewModelScope.launch { _profileImageStateFlow.emit(changes) }
        }

        acceptProfileLinkAction = { action ->
            val updatedProfileLinkEntries = when (action) {
                is ProfileLinkAction.Edit -> {
                    val entry = action.profileLinkEntry
                    profileLinkEntries.filter { it.site != entry.site } + entry
                }

                is ProfileLinkAction.Delete -> profileLinkEntries.filter { it.site != action.profileLinkEntry.site }
            }

            viewModelScope.launch {
                _profileLinkEntriesStateFlow.emit(updatedProfileLinkEntries)
            }
        }

        canUpdateProfileFlow = combine(
            profileStateFlow,
            profileImageStateFlow,
            profileLinkEntriesFlow,
            initialProfileLinksFlow
        ) { profileState, profileImageState, profileLinkEntries, initialProfileLinkEntries ->
            profileState != initialProfileState
                    || profileImageState != initialProfileImageState
                    || profileLinkEntries.toSet() != initialProfileLinkEntries.toSet()
        }

        val _loadingStateFlow = MutableSharedFlow<LoadingState>()
        loadingStateFlow = _loadingStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = LoadingState.NotLoading
            )
        acceptLoadingState = { loadingState ->
            viewModelScope.launch { _loadingStateFlow.emit(loadingState) }
        }

        searchProvider = SearchProvider(algoliaKeyRepository)

        user?.id?.let { initProfileLinks(it) }

        viewModelScope.launch {
            val initialProfileLinks = initialProfileLinksFlow.first()
            _profileLinkEntriesStateFlow.emit(initialProfileLinks + profileLinkEntries)
        }
    }

    fun hasUser() = userRepository.hasUser

    fun updateUserProfile(profileImages: ProfileImageContainer?) {
        val user = userRepository.user ?: return
        val changes = profileState
        val waifu = if (changes.character != null && changes.waifuOrHusbando.isNotBlank()) {
            Character(id = changes.character.id.toString(), null, null, null, null, null)
        } else {
            null
        }

        val updatedUserModel = User(
            id = user.id,
            location = changes.location,
            birthday = changes.birthday,
            gender = if (changes.gender == "custom") changes.customGender else changes.gender,
            waifuOrHusbando = changes.waifuOrHusbando,
            about = changes.about,
            waifu = waifu
        )

        if (user.id.isNullOrBlank()) return

        acceptLoadingState(LoadingState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (user.waifu != null && waifu == null) {
                    deleteWaifuRelationship(user.id)
                }

                if (profileImages != null) {
                    uploadUserImages(user.id, profileImages)
                }

                updateProfileLinks(user.id)

                val response = service.updateUser(user.id, JSONAPIDocument(updatedUserModel))

                if (response.get() != null) {
                    // request full user model to update local cached model
                    userRepository.updateUserCache()
                    acceptLoadingState(LoadingState.Success)
                } else {
                    throw ReceivedDataException("Received user data is null.")
                }
            } catch (e: Exception) {
                logE("Failed to update user profile.", e)
                acceptLoadingState(LoadingState.Error(e))
            }
        }
    }

    private fun deleteWaifuRelationship(userId: String) {
        logD("Deleting waifu relationship.")
        val responseWaifu = service.deleteWaifuRelationship(userId).execute()
        if (!responseWaifu.isSuccessful) {
            throw ReceivedDataException("Failed to delete waifu relationship.")
        }
    }

    private fun uploadUserImages(useId: String, profileImages: ProfileImageContainer) {
        logD("Updating user image(s).")
        val body = UserImageUpload(
            id = useId,
            avatar = profileImages.avatar,
            coverImage = profileImages.coverImage
        )

        val response = imageUploadService.updateUserImage(useId, JSONAPIDocument(body)).execute()
        if (!response.isSuccessful) {
            throw ReceivedDataException("Failed to update user image.")
        }
    }

    private suspend fun updateProfileLinks(userId: String) {
        val initialProfileLinks = initialProfileLinksFlow.replayCache.firstOrNull() ?: emptyList()

        val newProfileLinks = profileLinkEntries.filter { newEntry ->
            initialProfileLinks.none { it.site.id == newEntry.site.id }
        }.map { newEntry ->
            buildProfileLink(
                profileLinkId = null,
                url = newEntry.url,
                profileLinkSiteId = newEntry.site.id,
                userId = userId
            )
        }

        val updatedProfileLinks = profileLinkEntries.filter { newEntry ->
            newEntry.id != null && initialProfileLinks.none { it == newEntry }
        }.map { newEntry ->
            buildProfileLink(
                profileLinkId = newEntry.id,
                url = newEntry.url,
                profileLinkSiteId = newEntry.site.id,
                userId = userId
            )
        }

        val deletedProfileLinks = initialProfileLinks.filter { initialEntry ->
            initialEntry.id != null && profileLinkEntries.none { it.id == initialEntry.id }
        }

        withContext(Dispatchers.IO) {
            newProfileLinks.forEach { profileLink ->
                try {
                    val response = profileLinkService.createProfileLink(profileLink)
                    if (response.get() == null) {
                        throw ReceivedDataException("Received response is null.")
                    }
                } catch (e: Exception) {
                    logE("Failed to create profile link.", e)
                }
            }

            updatedProfileLinks.forEach { profileLink ->
                try {
                    val response = profileLinkService.updateProfileLink(
                        profileLink.id ?: return@forEach,
                        profileLink
                    )
                    if (response.get() == null) {
                        throw ReceivedDataException("Received response is null.")
                    }
                } catch (e: Exception) {
                    logE("Failed to update profile link.", e)
                }
            }

            deletedProfileLinks.forEach { profileLink ->
                try {
                    val response = profileLinkService.deleteProfileLink(
                        profileLink.id ?: return@forEach
                    ).execute()
                    if (!response.isSuccessful) {
                        throw ReceivedDataException("Failed to delete profile link.")
                    }
                } catch (e: Exception) {
                    logE("Failed to delete profile link.", e)
                }
            }
        }
    }

    private fun buildProfileLink(
        profileLinkId: String?,
        url: String?,
        profileLinkSiteId: String?,
        userId: String
    ): ProfileLink {
        return ProfileLink(
            id = profileLinkId,
            url = url,
            profileLinkSite = ProfileLinkSite(id = profileLinkSiteId, name = null),
            user = User(id = userId)
        )
    }

    fun initSearchClient() {
        if (searchProvider.isInitialized) return
        val characterSearchQuery = query {
            attributesToRetrieve {
                +"id"
                +"slug"
                +"canonicalName"
                +"image"
                +"primaryMedia"
            }
            responseFields {
                +Hits
            }
            attributesToHighlight { }
            removeStopWords = RemoveStopWords.False
            removeWordsIfNoResults = RemoveWordIfNoResults.AllOptional
        }
        createSearchClient(characterSearchQuery)
    }

    private fun createSearchClient(query: Query) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                searchProvider.createSearchClient(
                    SearchType.Characters,
                    query,
                    triggerSearchFor = { !it.query.isNullOrBlank() }
                ) { searcher ->
                    connectionHandler.clear()
                    val searchBoxConnector = SearchBoxConnector(searcher)
                    connectionHandler += searchBoxConnector
                    _searchBoxConnectorFlow.emit(searchBoxConnector)
                }
            } catch (e: Exception) {
                logE("Failed to create search client.", e)
            }
        }
    }

    private fun initProfileLinks(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            acceptLoadingState(LoadingState.Loading)
            val userProfileLinks = fetchUserProfileLinks(userId).mapNotNull { profileLink ->
                val url = profileLink.url ?: return@mapNotNull null
                val site = profileLink.profileLinkSite ?: return@mapNotNull null
                ProfileLinkEntry(profileLink.id, url, site)
            }
            initialProfileLinksFlow.emit(userProfileLinks)
            acceptLoadingState(LoadingState.NotLoading)
        }
    }

    private suspend fun fetchUserProfileLinks(userId: String): List<ProfileLink> {
        return try {
            val filter = Filter()
                .limit(50)
                .include("profileLinkSite")
            service.getProfileLinksForUser(userId, filter.options).get() ?: emptyList()
        } catch (e: Exception) {
            logE("Failed to fetch profile links for user.", e)
            emptyList()
        }
    }

    fun loadProfileLinkSites() {
        if (!profileLinkSites.isNullOrEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _profileLinkSitesLoadStateFlow.emit(true)
            val profileLinkSites = fetchProfileLinkSites()
            _profileLinkSitesFlow.emit(profileLinkSites)
            _profileLinkSitesLoadStateFlow.emit(false)
        }
    }

    private suspend fun fetchProfileLinkSites(): List<ProfileLinkSite> {
        return try {
            val filter = Filter().pageLimit(50)
            profileLinkService.allProfileLinkSites(filter.options).get() ?: emptyList()
        } catch (e: Exception) {
            logE("Failed to fetch profile link sites.", e)
            emptyList()
        }
    }

    private fun User.getGenderWithoutCustomGender(): String? {
        return when (gender) {
            null, "", "male", "female", "secret" -> gender
            else -> "custom"
        }
    }

    private fun User.getCustomGenderOrNull(): String? {
        return when (gender) {
            "male", "female", "secret" -> null
            else -> gender
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionHandler.clear()
        searchProvider.cancel()
    }
}

data class ProfileState(
    val location: String,
    val birthday: String,
    val gender: String,
    val customGender: String,
    val waifuOrHusbando: String,
    val character: Character?,
    val about: String
)

data class ProfileImageState(
    val currentAvatarUrl: String?,
    val currentCoverUrl: String?,
    val selectedAvatarUri: Uri? = null,
    val selectedCoverUri: Uri? = null
)

data class ProfileImageContainer(
    val avatar: String?,
    val coverImage: String?
)

@Parcelize
data class ProfileLinkEntry(
    val id: String? = null,
    val url: String,
    val site: ProfileLinkSite
) : Parcelable

sealed class ProfileLinkAction {
    data class Edit(val profileLinkEntry: ProfileLinkEntry) : ProfileLinkAction()
    data class Delete(val profileLinkEntry: ProfileLinkEntry) : ProfileLinkAction()
}

sealed class LoadingState {
    data object NotLoading : LoadingState()
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data class Error(val exception: Exception) : LoadingState()
}

enum class ImagePickerType {
    AVATAR, COVER
}
