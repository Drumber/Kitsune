package io.github.drumber.kitsune.data.model.streamer

import android.os.Parcelable
import com.github.jasminb.jsonapi.RelType
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("streamingLinks")
data class StreamingLink(
    @Id val id: String?,
    val url: String?,
    val subs: List<String>?,
    val dubs: List<String>?,
    @Relationship("streamer", resolve = true, relType = RelType.RELATED)
    val streamer: Streamer?
) : Parcelable
