package io.github.drumber.kitsune.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.presentation.model.group.GroupCategory
import io.github.drumber.kitsune.data.repository.GroupsRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsViewModel(
    private val groupsRepository: GroupsRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    private data class GroupsQuery(
        val query: String? = null,
        val categoryId: String? = null,
        val following: Boolean = false
    )

    val isLoggedIn: Boolean
        get() = getLocalUserId() != null

    private val groupsQuery = MutableStateFlow(GroupsQuery(following = isLoggedIn))

    private val _categories = MutableStateFlow<List<GroupCategory>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _isFollowingEnabled = MutableStateFlow(isLoggedIn)
    val isFollowingEnabled = _isFollowingEnabled.asStateFlow()

    /** Raw text of the search field, kept here so it survives configuration changes. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val dataSource: Flow<PagingData<Group>> = groupsQuery.flatMapLatest { q ->
        val userId = getLocalUserId()
        if (q.following && userId != null) {
            groupsRepository.followedGroupsPager(userId = userId, query = q.query)
        } else {
            groupsRepository.groupsPager(query = q.query, categoryId = q.categoryId)
        }
    }.cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    fun setSearchQuery(query: String?) {
        _searchQuery.value = query.orEmpty()
        val trimmed = query?.trim()?.takeUnless { it.isBlank() }
        if (groupsQuery.value.query != trimmed) {
            groupsQuery.value = groupsQuery.value.copy(query = trimmed)
        }
    }

    fun setCategory(categoryId: String?) {
        if (_selectedCategoryId.value != categoryId) {
            _selectedCategoryId.value = categoryId
            groupsQuery.value = groupsQuery.value.copy(categoryId = categoryId)
        }
    }

    fun setFollowingEnabled(enabled: Boolean) {
        if (_isFollowingEnabled.value != enabled) {
            _isFollowingEnabled.value = enabled
            groupsQuery.value = groupsQuery.value.copy(following = enabled)
        }
    }

    private val _scrollToTopRequested = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTopRequested: SharedFlow<Unit> = _scrollToTopRequested.asSharedFlow()

    fun requestScrollToTop() {
        _scrollToTopRequested.tryEmit(Unit)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = groupsRepository.getCategories()
            } catch (e: Exception) {
                logE("Failed to load group categories.", e)
            }
        }
    }

}
