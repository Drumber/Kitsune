package io.github.drumber.kitsune.domain.model.ui.media

import io.github.drumber.kitsune.domain.model.infrastructure.image.Image

fun Image.smallOrHigher(): String? {
    return small ?: medium ?: large ?: original
}

fun Image.originalOrDown(): String? {
    return original ?: large ?: medium ?: small ?: tiny
}
