package io.github.drumber.kitsune.data.model.production

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.media.Image
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("characters")
data class Character(
    @Id val id: String?,
    val slug: String?,
    val name: String?,
    val malId: Int?,
    val description: String?,
    val image: Image?
) : Parcelable
