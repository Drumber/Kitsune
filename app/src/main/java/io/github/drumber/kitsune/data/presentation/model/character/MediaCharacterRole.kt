package io.github.drumber.kitsune.data.presentation.model.character

import androidx.annotation.StringRes
import io.github.drumber.kitsune.R

enum class MediaCharacterRole {
    MAIN,
    SUPPORTING,
    RECURRING,
    CAMEO
}

@StringRes
fun MediaCharacterRole.getStringRes() = when (this) {
    MediaCharacterRole.MAIN -> R.string.character_role_main
    MediaCharacterRole.SUPPORTING -> R.string.character_role_supporting
    MediaCharacterRole.RECURRING -> R.string.character_role_recurring
    MediaCharacterRole.CAMEO -> R.string.character_role_cameo
}
