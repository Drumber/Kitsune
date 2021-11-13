package io.github.drumber.kitsune.data.model.streamer

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("streamers")
data class Streamer(
    @Id val id: String?,
    val siteName: String?,
) : Parcelable
