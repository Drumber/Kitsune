package io.github.drumber.kitsune.domain.model.infrastructure.character

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.common.media.Titles
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.user.FavoriteItem
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("characters")
data class Character(
    @Id
    val id: String?,
    val slug: String?,
    val name: String?,
    val names: Titles?,
    val otherNames: List<String>?,
    val malId: Int?,
    val description: String?,
    val image: Image?,

    @Relationship("mediaCharacters")
    val mediaCharacters: List<MediaCharacter>? = null
) : FavoriteItem
