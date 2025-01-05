package io.github.drumber.kitsune.data.source.jsonapi.user.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("users")
data class NetworkUserImageUpload(
    @Id
    val id: String?,
    /** Avatar image as Base64 encoded string */
    val avatar: String? = null,
    /** Cover image as Base64 encoded string */
    val coverImage: String? = null
)
