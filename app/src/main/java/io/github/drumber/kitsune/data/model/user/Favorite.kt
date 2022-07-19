package io.github.drumber.kitsune.data.model.user

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("favorites")
data class Favorite(
    @Id val id: String? = null,
    val favRank: Int? = null,
    @Relationship("item")
    val item: FavoriteItem? = null,
    @Relationship("user")
    val user: User? = null
) : Parcelable

interface FavoriteItem : Parcelable
