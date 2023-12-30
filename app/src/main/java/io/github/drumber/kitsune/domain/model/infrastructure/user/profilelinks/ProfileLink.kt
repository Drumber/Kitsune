package io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("profileLinks")
data class ProfileLink(
    @Id
    val id: String?,
    val url: String?,
    @Relationship("profileLinkSite")
    val profileLinkSite: ProfileLinkSite?,
    @Relationship("user")
    val user: User?
) : Parcelable
