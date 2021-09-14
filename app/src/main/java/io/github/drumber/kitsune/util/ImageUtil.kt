package io.github.drumber.kitsune.util

import io.github.drumber.kitsune.data.model.resource.Image

fun Image.smallOrHigher(): String? {
    return small ?: medium ?: large ?: original
}

fun Image.originalOrDown(): String? {
    return original ?: large ?: medium ?: small ?: tiny
}
