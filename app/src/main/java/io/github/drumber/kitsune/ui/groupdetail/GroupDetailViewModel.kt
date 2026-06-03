package io.github.drumber.kitsune.ui.groupdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.repository.GroupsRepository
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val groupId: String,
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _group = MutableStateFlow<Group?>(null)
    val group = _group.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadGroup()
    }

    private fun loadGroup() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _group.value = groupsRepository.getGroup(groupId)
            } catch (e: Exception) {
                logE("Failed to load group with id '$groupId'.", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

}
