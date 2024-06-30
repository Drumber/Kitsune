package io.github.drumber.kitsune.data.presentation.model.user.profilelinks

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileLinkSite(
    val id: String,
    val name: String?
) : Parcelable
