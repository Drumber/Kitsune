package io.github.drumber.kitsune.domain.model.infrastructure.media.unit

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.media.Titles

interface MediaUnit : Parcelable {
    val id: String?

    val description: String?
    val titles: Titles?
    val canonicalTitle: String?

    val number: Int?
    val length: String?
    val thumbnail: Image?
}
