package io.github.drumber.kitsune.data.model.resource

import android.os.Parcelable

interface MediaUnit : Parcelable {
    val id: String?
    val description: String?
    val titles: Titles?
    val canonicalTitle: String?
    val number: Int?
    val length: String?
    val thumbnail: Image?
}