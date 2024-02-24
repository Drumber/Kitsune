package io.github.drumber.kitsune.domain.model.infrastructure.production

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.character.Character
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("castings")
data class Casting(
    @Id
    val id: String?,
    val role: String?,
    val voiceActor: Boolean?,
    val featured: Boolean?,
    val language: String?,

    @Relationship("character")
    val character: Character?,
    @Relationship("person")
    val person: Person?
) : Parcelable
