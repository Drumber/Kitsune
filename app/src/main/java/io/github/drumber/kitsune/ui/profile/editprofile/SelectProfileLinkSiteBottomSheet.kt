package io.github.drumber.kitsune.ui.profile.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.ui.compose.composeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectProfileLinkSiteBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: EditProfileViewModel by viewModel(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val profileLinkSites by viewModel.profileLinkSitesFlow.collectAsStateWithLifecycle(
            initialValue = emptyList()
        )
        val isLoading by viewModel.profileLinkSitesLoadStateFlow.collectAsStateWithLifecycle()

        // Filter out sites that already have a link added
        val availableSites = profileLinkSites.filter { site ->
            viewModel.profileLinkEntries.none { it.site.id == site.id }
        }

        SelectProfileLinkSiteScreen(
            profileLinkSites = availableSites,
            isLoading = isLoading,
            onSiteSelected = { site -> onSiteSelected(site) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProfileLinkSites()
    }

    private fun onSiteSelected(linkSite: ProfileLinkSite) {
        setFragmentResult(
            PROFILE_SITE_SELECTED_REQUEST_KEY,
            bundleOf(BUNDLE_PROFILE_LINK_SITE to linkSite)
        )
        dismiss()
    }

    companion object {
        const val TAG = "select_profile_link_site_bottom_sheet"
        const val BUNDLE_PROFILE_LINK_SITE = "profile_link_site_bundle_key"
        const val PROFILE_SITE_SELECTED_REQUEST_KEY = "site_selected_request_key"
    }
}
