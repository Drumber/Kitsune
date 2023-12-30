package io.github.drumber.kitsune.ui.profile.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.databinding.ItemListOptionBinding
import io.github.drumber.kitsune.databinding.SheetSelectProfileLinkSiteBinding
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.util.ItemClickListener
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectProfileLinkSiteBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: EditProfileViewModel by viewModel(ownerProducer = { requireParentFragment() })

    private var _binding: SheetSelectProfileLinkSiteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetSelectProfileLinkSiteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProfileLinkSites()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileLinkSitesFlow.collectLatest { profileLinkSites ->
                val addedProfileLinks = viewModel.profileLinkEntries
                val profileLinksWithoutAddedEntries = profileLinkSites.filter { profileLinkSite ->
                    addedProfileLinks.none { it.site.id == profileLinkSite.id }
                }
                updateProfileLinkSites(profileLinksWithoutAddedEntries)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileLinkSitesLoadStateFlow.collectLatest {
                binding.progressBarProfileLinkSites.isVisible = it
            }
        }
    }

    private fun updateProfileLinkSites(linkSites: List<ProfileLinkSite>) {
        binding.layoutListParent.removeAllViews()
        linkSites.forEach { linkSite ->
            if (linkSite.name.isNullOrBlank()) return@forEach
            val itemBinding =
                ItemListOptionBinding.inflate(layoutInflater, binding.layoutListParent, true)
            itemBinding.title = linkSite.name
            itemBinding.icon = ResourcesCompat.getDrawable(
                resources,
                getProfileSiteLogoResourceId(linkSite.name),
                activity?.theme
            )
            itemBinding.listener = ItemClickListener { onItemClicked(linkSite) }
        }
    }

    private fun onItemClicked(linkSite: ProfileLinkSite) {
        setFragmentResult(
            PROFILE_SITE_SELECTED_REQUEST_KEY,
            bundleOf(BUNDLE_PROFILE_LINK_SITE to linkSite)
        )
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "select_profile_link_site_bottom_sheet"
        const val BUNDLE_PROFILE_LINK_SITE = "profile_link_site_bundle_key"
        const val PROFILE_SITE_SELECTED_REQUEST_KEY = "site_selected_request_key"
    }

}