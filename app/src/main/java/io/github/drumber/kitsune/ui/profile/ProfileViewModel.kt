package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class ProfileViewModel(
    userRepository: UserRepository,
    private val libraryEntriesRepository: LibraryEntriesRepository
) : ViewModel() {

    val userModel: LiveData<User?> = Transformations.map(userRepository.userLiveData) { it }

    private val _filter = Transformations.map(userRepository.userLiveData) { user ->
        user?.id?.let { userId ->
            Filter()
                .filter("user_id", userId)
                .sort("status", "-progressed_at")
                .include("anime", "manga")
        }
    }
    val filter: LiveData<Filter>
        get() = _filter

    val dataSource: Flow<PagingData<LibraryEntry>> = filter.asFlow().flatMapLatest { filter ->
        libraryEntriesRepository.libraryEntries(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter)
    }.cachedIn(viewModelScope)

}