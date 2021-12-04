package io.github.drumber.kitsune.data.model.media

import android.os.Parcelable

sealed class BaseMedia : Media, Parcelable

interface Media : Parcelable {
    val id: String
}
