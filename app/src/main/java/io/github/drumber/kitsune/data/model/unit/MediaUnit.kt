package io.github.drumber.kitsune.data.model.unit

import android.os.Parcelable
import io.github.drumber.kitsune.data.model.media.Image
import io.github.drumber.kitsune.data.model.media.Titles

interface MediaUnit : Parcelable {
    val id: String?
    val description: String?
    val titles: Titles?
    val canonicalTitle: String?
    val number: Int?
    val length: String?
    val thumbnail: Image?
}