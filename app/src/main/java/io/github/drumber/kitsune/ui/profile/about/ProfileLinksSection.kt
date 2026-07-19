package io.github.drumber.kitsune.ui.profile.about

import android.view.LayoutInflater
import androidx.core.view.isVisible
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.databinding.FragmentProfileAboutBinding
import io.github.drumber.kitsune.databinding.ItemProfileSiteChipBinding
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId

/**
 * Encapsulates the profile links chip group section. Used by both [io.github.drumber.kitsune.ui.profile.MyProfileFragment] and
 * [io.github.drumber.kitsune.ui.profile.UserProfileFragment], which differ only in their profile-link click callback.
 */
class ProfileLinksSection(
    private val binding: FragmentProfileAboutBinding,
    private val layoutInflater: LayoutInflater,
    private val onProfileLinkClick: (ProfileLink) -> Unit
) {

    fun submitProfileLinks(profileLinks: List<ProfileLink>) {
        binding.scrollViewProfileLinks.isVisible = profileLinks.isNotEmpty()
        binding.chipGroupProfileLinks.apply {
            removeAllViews()

            profileLinks.sortedBy { it.profileLinkSite?.id?.toIntOrNull() }
                .forEach { profileLink ->
                    val profileLinkBinding = ItemProfileSiteChipBinding.inflate(layoutInflater, this, true)
                    val chip = profileLinkBinding.root
                    val siteName = profileLink.profileLinkSite?.name
                    chip.text = siteName
                    chip.setChipIconResource(getProfileSiteLogoResourceId(siteName))
                    chip.setOnClickListener { onProfileLinkClick(profileLink) }
                }
        }
    }
}
