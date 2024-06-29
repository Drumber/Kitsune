package io.github.drumber.kitsune.domain_old.model.infrastructure.media.streamer

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("streamingLinks")
data class StreamingLink(
    @Id
    val id: String?,
    val url: String?,
    val subs: List<String>?,
    val dubs: List<String>?,

    @Relationship("streamer")
    val streamer: Streamer?
) : Parcelable
