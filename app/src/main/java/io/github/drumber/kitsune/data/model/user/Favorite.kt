package io.github.drumber.kitsune.data.model.user

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.media.Media
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("favorites")
data class Favorite(
    @Id val id: String? = null,
    val favRank: Int? = null,
    @Relationship("item")
    val item: Media? = null,
    @Relationship("user")
    val user: User? = null
) : Parcelable
