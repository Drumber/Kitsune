package io.github.drumber.kitsune.ui.profile.editprofile

import android.net.Uri
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
import io.github.drumber.kitsune.domain.model.ui.media.originalOrDown
import io.github.drumber.kitsune.domain.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.domain.service.user.UserImageUploadService
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository,
    algoliaKeyRepository: AlgoliaKeyRepository,
    private val service: UserService,
    private val imageUploadService: UserImageUploadService
) : ViewModel() {

    val loadingStateFlow: StateFlow<LoadingState>

    val profileStateFlow: StateFlow<ProfileState>
    val profileImageStateFlow: StateFlow<ProfileImageState>
    val canUpdateProfileFlow: Flow<Boolean>

    val profileState
        get() = profileStateFlow.value

    val profileImageState
        get() = profileImageStateFlow.value

    val acceptProfileChanges: (ProfileState) -> Unit
    val acceptProfileImageChanges: (ProfileImageState) -> Unit

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

        acceptProfileChanges = { changes ->
            viewModelScope.launch { _profileStateFlow.emit(changes) }
        }

        acceptProfileImageChanges = { changes ->
            viewModelScope.launch { _profileImageStateFlow.emit(changes) }
        }

        canUpdateProfileFlow = combine(
            profileStateFlow,
            profileImageStateFlow
        ) { profileState, profileImageState ->
            profileState != initialProfileState || profileImageState != initialProfileImageState
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
                    logD("Deleting waifu relationship.")
                    val responseWaifu = service.deleteWaifuRelationship(user.id).execute()
                    if (!responseWaifu.isSuccessful) {
                        throw ReceivedDataException("Failed to delete waifu relationship.")
                    }
                }

                if (profileImages != null) {
                    logD("Updating user image(s).")
                    val body = UserImageUpload(
                        id = user.id,
                        avatar = profileImages.avatar,
                        coverImage = profileImages.coverImage
                    )

                    val response =
                        imageUploadService.updateUserImage(user.id, JSONAPIDocument(body)).execute()
                    if (!response.isSuccessful) {
                        throw ReceivedDataException("Failed to update user image.")
                    }
                }

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

sealed class LoadingState {
    data object NotLoading : LoadingState()
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data class Error(val exception: Exception) : LoadingState()
}

enum class ImagePickerType {
    AVATAR, COVER
}
