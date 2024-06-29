package io.github.drumber.kitsune.domain_old.model.infrastructure.character

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain_old.model.common.media.Titles
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.FavoriteItem
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("characters")
data class Character(
    @Id
    val id: String?,
    val slug: String? = null,
    val name: String? = null,
    val names: Titles? = null,
    val otherNames: List<String>? = null,
    val malId: Int? = null,
    val description: String? = null,
    val image: Image? = null,

    @Relationship("mediaCharacters")
    val mediaCharacters: List<MediaCharacter>? = null
) : FavoriteItem
