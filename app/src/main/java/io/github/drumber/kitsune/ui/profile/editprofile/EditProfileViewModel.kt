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
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.mapper.CharacterMapper.toCharacter
import io.github.drumber.kitsune.data.presentation.model.algolia.SearchType
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.ProfileLinkRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.character.LocalCharacter
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.domain.algolia.SearchProvider
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
    private val profileLinkRepository: ProfileLinkRepository,
    algoliaKeyRepository: AlgoliaKeyRepository
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
        val user = userRepository.localUser.value

        val initialProfileState = ProfileState(
            location = user?.location ?: "",
            birthday = user?.birthday ?: "",
            gender = user?.getGenderWithoutCustomGender() ?: "",
            customGender = user?.getCustomGenderOrNull() ?: "",
            waifuOrHusbando = user?.waifuOrHusbando ?: "",
            character = user?.waifu?.toCharacter(),
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
                started = SharingStarted.Lazily,
                initialValue = initialProfileState
            )

        val _profileImageStateFlow = MutableSharedFlow<ProfileImageState>()
        profileImageStateFlow = _profileImageStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = initialProfileImageState
            )

        val _profileLinkEntriesStateFlow = MutableSharedFlow<List<ProfileLinkEntry>>()
        profileLinkEntriesFlow = _profileLinkEntriesStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
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
                    val initialEntry = initialProfileLinksFlow.replayCache.firstOrNull()
                        ?.firstOrNull { it.site.id == action.profileLinkEntry.site.id }

                    val entry = initialEntry?.copy(url = action.profileLinkEntry.url)
                        ?: action.profileLinkEntry
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
                started = SharingStarted.Lazily,
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

    fun hasUser() = userRepository.hasLocalUser()

    fun updateUserProfile(profileImages: ProfileImageContainer?) {
        val user = userRepository.localUser.value ?: return
        val changes = profileState
        val waifu = if (changes.character != null && changes.waifuOrHusbando.isNotBlank()) {
            LocalCharacter.empty(changes.character.id)
        } else {
            null
        }

        val updatedUserModel = LocalUser.empty(user.id).copy(
            location = changes.location,
            birthday = changes.birthday,
            gender = if (changes.gender == "custom") changes.customGender else changes.gender,
            waifuOrHusbando = changes.waifuOrHusbando,
            about = changes.about,
            waifu = waifu
        )

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

                val response = userRepository.updateUser(user.id, updatedUserModel)

                if (response != null) {
                    // request full user model to update local cached model
                    userRepository.fetchAndStoreLocalUserFromNetwork()
                    acceptLoadingState(LoadingState.Success)
                } else {
                    throw ProfileUpdateException.ProfileDataError(ProfileDataErrorType.UpdateProfile)
                }
            } catch (e: Exception) {
                logE("Failed to update user profile.", e)
                acceptLoadingState(LoadingState.Error(e))
            }
        }
    }

    private suspend fun deleteWaifuRelationship(userId: String) {
        logD("Deleting waifu relationship.")
        val isSuccessful = userRepository.deleteWaifuRelationship(userId)
        if (!isSuccessful) {
            throw ProfileUpdateException.ProfileDataError(ProfileDataErrorType.DeleteWaifu)
        }
    }

    private suspend fun uploadUserImages(useId: String, profileImages: ProfileImageContainer) {
        logD("Updating user image(s).")
        val isSuccessful = userRepository.updateUserImage(useId, profileImages.avatar, profileImages.coverImage)
        if (!isSuccessful) {
            throw ProfileUpdateException.ProfileImageError()
        }
    }

    private suspend fun updateProfileLinks(userId: String) = withContext(Dispatchers.IO) {
        val initialProfileLinks = initialProfileLinksFlow.replayCache.firstOrNull() ?: emptyList()

        val newProfileLinks = profileLinkEntries.filter { newEntry ->
            initialProfileLinks.none { it.site.id == newEntry.site.id }
        }

        val updatedProfileLinks = profileLinkEntries.filter { newEntry ->
            newEntry.id != null && initialProfileLinks.none { it == newEntry }
        }

        val deletedProfileLinks = initialProfileLinks.filter { initialEntry ->
            initialEntry.id != null && profileLinkEntries.none { it.site.id == initialEntry.site.id }
        }

        newProfileLinks.forEach { profileLinkEntry ->
            try {
                val createdProfileLink = createProfileLink(profileLinkEntry, userId)
                    ?: throw NoDataException("Received response is null.")
                addOrUpdateInitialProfileLink(profileLinkEntry, createdProfileLink)
            } catch (e: Exception) {
                logE("Failed to create profile link $profileLinkEntry.", e)
                throw ProfileUpdateException.ProfileLinkError(
                    ProfileLinkOperation.Create,
                    profileLinkEntry
                )
            }
        }

        updatedProfileLinks.forEach { profileLinkEntry ->
            try {
                val updatedProfileLink = updateProfileLink(profileLinkEntry, userId)
                    ?: throw NoDataException("Received response is null.")
                addOrUpdateInitialProfileLink(profileLinkEntry, updatedProfileLink)
            } catch (e: Exception) {
                logE("Failed to update profile link $profileLinkEntry.", e)
                throw ProfileUpdateException.ProfileLinkError(
                    ProfileLinkOperation.Update,
                    profileLinkEntry
                )
            }
        }

        deletedProfileLinks.forEach { profileLinkEntry ->
            try {
                val isSuccessful = profileLinkRepository.deleteProfileLink(profileLinkEntry.id!!)
                if (!isSuccessful) {
                    throw NoDataException("Failed to delete profile link.")
                }
                removeFromInitialProfileLinks(profileLinkEntry)
            } catch (e: Exception) {
                logE("Failed to delete profile link $profileLinkEntry.", e)
                throw ProfileUpdateException.ProfileLinkError(
                    ProfileLinkOperation.Delete,
                    profileLinkEntry
                )
            }
        }
    }

    private suspend fun createProfileLink(
        profileLinkEntry: ProfileLinkEntry,
        userId: String
    ): ProfileLink? {
        return profileLinkRepository.createProfileLink(
            userId,
            profileLinkEntry.site.id,
            profileLinkEntry.url
        )
    }

    private suspend fun updateProfileLink(
        profileLinkEntry: ProfileLinkEntry,
        userId: String
    ): ProfileLink? {
        return profileLinkRepository.updateProfileLink(
            userId,
            profileLinkEntry.id!!,
            profileLinkEntry.url
        )
    }

    private fun addOrUpdateInitialProfileLink(
        localProfileLinkEntry: ProfileLinkEntry,
        remoteProfileLink: ProfileLink
    ) {
        val initialProfileLinkEntry = initialProfileLinksFlow.replayCache.firstOrNull()
            ?.find { it.id == remoteProfileLink.id || it.site.id == remoteProfileLink.profileLinkSite?.id }

        val updatedProfileLinkEntry = when (initialProfileLinkEntry) {
            // add new profile link entry to initialProfileLinks
            null -> localProfileLinkEntry.copy(
                id = remoteProfileLink.id,
                url = remoteProfileLink.url ?: localProfileLinkEntry.url,
                site = remoteProfileLink.profileLinkSite ?: localProfileLinkEntry.site
            )

            // update initialProfileLinkEntry with remoteProfileLink
            else -> initialProfileLinkEntry.copy(
                id = remoteProfileLink.id,
                url = remoteProfileLink.url ?: localProfileLinkEntry.url
            )
        }
        val initialProfileLinksWithoutEntry = getInitialProfileLinksWithout(remoteProfileLink)

        viewModelScope.launch {
            initialProfileLinksFlow.emit(initialProfileLinksWithoutEntry + updatedProfileLinkEntry)
        }
    }

    private fun removeFromInitialProfileLinks(profileLinkEntry: ProfileLinkEntry) {
        val initialProfileLinksWithoutEntry = getInitialProfileLinksWithout(
            ProfileLink(
                id = profileLinkEntry.id ?: "",
                url = profileLinkEntry.url,
                profileLinkSite = profileLinkEntry.site,
                user = null
            )
        )
        viewModelScope.launch {
            initialProfileLinksFlow.emit(initialProfileLinksWithoutEntry)
        }
    }

    private fun getInitialProfileLinksWithout(profileLink: ProfileLink): List<ProfileLinkEntry> {
        return initialProfileLinksFlow.replayCache.firstOrNull()
            ?.filterNot { it.id == profileLink.id || it.site.id == profileLink.profileLinkSite?.id }
            ?: emptyList()
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
            userRepository.getProfileLinksForUser(userId, filter) ?: emptyList()
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
            profileLinkRepository.getAllProfileLinkSites(filter) ?: emptyList()
        } catch (e: Exception) {
            logE("Failed to fetch profile link sites.", e)
            emptyList()
        }
    }

    private fun LocalUser.getGenderWithoutCustomGender(): String? {
        return when (gender) {
            null, "", "male", "female", "secret" -> gender
            else -> "custom"
        }
    }

    private fun LocalUser.getCustomGenderOrNull(): String? {
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
    data class Error(val exception: Exception, var isConsumed: Boolean = false) : LoadingState()
}

sealed class ProfileUpdateException : Exception() {
    class ProfileDataError(val type: ProfileDataErrorType) : ProfileUpdateException() {
        override val message: String
            get() = "Failed to update profile data. Type: $type"
    }

    class ProfileImageError : ProfileUpdateException()

    class ProfileLinkError(
        val operation: ProfileLinkOperation,
        val profileLinkEntry: ProfileLinkEntry
    ) : ProfileUpdateException() {
        override val message: String
            get() = "Failed to $operation profile link $profileLinkEntry."
    }
}

enum class ProfileDataErrorType {
    UpdateProfile, DeleteWaifu
}

enum class ProfileLinkOperation {
    Create, Update, Delete
}

enum class ImagePickerType {
    AVATAR, COVER
}
