package io.github.drumber.kitsune.ui.groupdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.repository.GroupsRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val groupId: String,
    private val groupsRepository: GroupsRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface Event {
        data object LoginRequired : Event
        data object JoinFailed : Event
        data object LeaveFailed : Event
    }

    private val _group = MutableStateFlow<Group?>(null)
    val group = _group.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _membershipState = MutableStateFlow(MembershipState())
    val membershipState = _membershipState.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        loadGroup()
        loadMembership()
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

    private fun loadMembership() {
        val userId = getLocalUserId() ?: run {
            _membershipState.value = MembershipState(isVisible = false)
            return
        }
        viewModelScope.launch {
            try {
                val membershipId = groupsRepository.getMembershipId(groupId, userId)
                _membershipState.value = MembershipState(
                    isVisible = true,
                    isMember = membershipId != null,
                    membershipId = membershipId
                )
            } catch (e: Exception) {
                logE("Failed to load membership for group '$groupId'.", e)
                _membershipState.value = MembershipState(isVisible = true)
            }
        }
    }

    fun toggleMembership() {
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }
        val current = _membershipState.value
        if (current.isLoading) return

        if (current.isMember) {
            leaveGroup(current.membershipId)
        } else {
            joinGroup(userId)
        }
    }

    private fun joinGroup(userId: String) {
        viewModelScope.launch {
            _membershipState.value = _membershipState.value.copy(isLoading = true)
            try {
                val membershipId = groupsRepository.joinGroup(groupId, userId)
                if (membershipId != null) {
                    _membershipState.value = _membershipState.value.copy(
                        isLoading = false,
                        isMember = true,
                        membershipId = membershipId
                    )
                } else {
                    _membershipState.value = _membershipState.value.copy(isLoading = false)
                    eventChannel.send(Event.JoinFailed)
                }
            } catch (e: Exception) {
                logE("Failed to join group '$groupId'.", e)
                _membershipState.value = _membershipState.value.copy(isLoading = false)
                eventChannel.send(Event.JoinFailed)
            }
        }
    }

    private fun leaveGroup(membershipId: String?) {
        if (membershipId == null) return
        viewModelScope.launch {
            _membershipState.value = _membershipState.value.copy(isLoading = true)
            try {
                val success = groupsRepository.leaveGroup(membershipId)
                if (success) {
                    _membershipState.value = _membershipState.value.copy(
                        isLoading = false,
                        isMember = false,
                        membershipId = null
                    )
                } else {
                    _membershipState.value = _membershipState.value.copy(isLoading = false)
                    eventChannel.send(Event.LeaveFailed)
                }
            } catch (e: Exception) {
                logE("Failed to leave group '$groupId'.", e)
                _membershipState.value = _membershipState.value.copy(isLoading = false)
                eventChannel.send(Event.LeaveFailed)
            }
        }
    }

    data class MembershipState(
        val isVisible: Boolean = false,
        val isMember: Boolean = false,
        val isLoading: Boolean = false,
        val membershipId: String? = null
    )

}
