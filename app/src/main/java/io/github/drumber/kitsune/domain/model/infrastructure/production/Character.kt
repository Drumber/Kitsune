package io.github.drumber.kitsune.domain.model.infrastructure.production

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
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
    val malId: Int?,
    val description: String?,
    val image: Image?
) : FavoriteItem
