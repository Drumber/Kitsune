package io.github.drumber.kitsune.data.model.production

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("castings")
data class Casting(
    @Id val id: String?,
    val role: String?,
    val voiceActor: Boolean?,
    val featured: Boolean?,
    val language: String?,
    @Relationship("character")
    val character: Character?
) : Parcelable
