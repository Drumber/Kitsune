package io.github.drumber.kitsune.domain.model.infrastructure.user

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("users")
data class UserImageUpload(
    @Id
    val id: String?,
    /** Avatar image as Base64 encoded string */
    val avatar: String? = null,
    /** Cover image as Base64 encoded string */
    val coverImage: String? = null
)
