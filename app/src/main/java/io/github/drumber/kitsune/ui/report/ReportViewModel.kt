package io.github.drumber.kitsune.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.presentation.model.report.ReportReason
import io.github.drumber.kitsune.data.presentation.model.report.ReportTarget
import io.github.drumber.kitsune.data.repository.ReportRepository
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val itemId: String,
    private val type: ReportTarget,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState(ReportState.Loading))
    val uiState = _uiState.asStateFlow()

    private val _submitEvent = MutableSharedFlow<SubmitEvent>()
    val submitEvent = _submitEvent.asSharedFlow()

    val canSubmit = uiState.map { it.canSubmit() }

    init {
        checkReportStatus(itemId)
    }

    fun getReportType() = type

    fun selectReason(reason: ReportReason) {
        _uiState.update { it.copy(selectedReason = reason) }
    }

    fun setExplanation(explanation: String?) {
        _uiState.update {
            it.copy(
                explanation = explanation?.trim()
                    ?.takeIf { explanation -> explanation.isNotBlank() })
        }
    }

    fun submitReport() {
        val state = uiState.value
        if (!state.canSubmit()) return
        val reason = state.selectedReason ?: return
        val explanation = state.explanation

        viewModelScope.launch {
            _uiState.update { it.copy(state = ReportState.Loading) }
            try {
                val success = reportRepository.submitReport(itemId, type, reason, explanation)
                if (success) {
                    _submitEvent.emit(SubmitEvent.ReportSent)
                } else {
                    _uiState.update { it.copy(state = ReportState.NotReported) }
                    _submitEvent.emit(SubmitEvent.Error)
                }
            } catch (e: Exception) {
                logE("Failed to submit report.", e)
                _uiState.update { it.copy(state = ReportState.NotReported) }
                _submitEvent.emit(SubmitEvent.Error)
            }
        }
    }

    private fun checkReportStatus(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(state = ReportState.Loading) }
            try {
                val alreadyReported = reportRepository.hasAlreadyReported(postId, type)
                val state = if (alreadyReported) {
                    ReportState.AlreadyReported
                } else {
                    ReportState.NotReported
                }
                _uiState.update { it.copy(state = state) }
            } catch (e: Exception) {
                logE("Failed to check report state for post $postId.", e)
                _uiState.update { it.copy(state = ReportState.NotReported) }
            }
        }
    }

    private fun UiState.canSubmit() = state == ReportState.NotReported
            && selectedReason != null
            && (!explanation.isNullOrBlank() || selectedReason != ReportReason.OTHER)

    data class UiState(
        val state: ReportState,
        val selectedReason: ReportReason? = null,
        val explanation: String? = null,
    )

    sealed interface ReportState {
        object Loading : ReportState
        object NotReported : ReportState
        object AlreadyReported : ReportState
    }

    sealed interface SubmitEvent {
        object ReportSent : SubmitEvent
        object Error : SubmitEvent
    }
}
