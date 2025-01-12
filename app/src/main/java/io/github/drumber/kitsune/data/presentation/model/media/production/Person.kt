package io.github.drumber.kitsune.data.presentation.model.media.production

import io.github.drumber.kitsune.data.model.Image

data class Person(
    val id: String,
    val name: String?,
    val description: String?,
    val image: Image?
)
