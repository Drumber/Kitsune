package io.github.drumber.kitsune.data.presentation.model.appupdate

sealed class UpdateCheckResult {
    data object NoNewVersion : UpdateCheckResult()
    data class NewVersion(val release: AppRelease) : UpdateCheckResult()
    data class Error(val exception: Exception) : UpdateCheckResult()
}