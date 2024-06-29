package io.github.drumber.kitsune.domain_old.model.infrastructure.character

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.FavoriteItem
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("mediaCharacters")
data class MediaCharacter(
    @Id
    val id: String?,
    val role: MediaCharacterRole?,

    @Relationship("media")
    val media: BaseMedia?
) : FavoriteItem
