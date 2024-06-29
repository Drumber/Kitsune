package io.github.drumber.kitsune.domain_old.model.infrastructure.user.profilelinks

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("profileLinkSites")
data class ProfileLinkSite(
    @Id
    val id: String?,
    val name: String?
) : Parcelable
