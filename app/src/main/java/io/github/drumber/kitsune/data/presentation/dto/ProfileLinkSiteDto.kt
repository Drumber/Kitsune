package io.github.drumber.kitsune.data.presentation.dto

import android.os.Parcelable
import io.github.drumber.kitsune.data.model.user.profilelinks.ProfileLinkSite
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileLinkSiteDto(
    val id: String,
    val name: String?
) : Parcelable

fun ProfileLinkSite.toProfileLinkSiteDto() = ProfileLinkSiteDto(
    id = id,
    name = name
)

fun ProfileLinkSiteDto.toProfileLinkSite() = ProfileLinkSite(
    id = id,
    name = name
)
