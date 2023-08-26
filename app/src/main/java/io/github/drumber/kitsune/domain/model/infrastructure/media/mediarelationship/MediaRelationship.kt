package io.github.drumber.kitsune.domain.model.infrastructure.media.mediarelationship

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.media.Media
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("mediaRelationships")
data class MediaRelationship(
    @Id
    val id: String?,
    val role: MediaRelationshipRole?,

    @Relationship("destination")
    val media: Media?
) : Parcelable
