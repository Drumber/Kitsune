package io.github.drumber.kitsune.domain.model.ui.media

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.infrastructure.character.MediaCharacterRole

fun MediaCharacterRole.getString(context: Context) = when (this) {
    MediaCharacterRole.MAIN -> R.string.character_role_main
    MediaCharacterRole.SUPPORTING -> R.string.character_role_supporting
    MediaCharacterRole.RECURRING -> R.string.character_role_recurring
    MediaCharacterRole.CAMEO -> R.string.character_role_cameo
}.let { context.getString(it) }
