package io.github.drumber.kitsune.ui.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.library.LibraryEntryAdapter
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.model.library.getStringResId
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.details.DetailsViewModel.ErrorResponseType
import io.github.drumber.kitsune.ui.details.tabs.DetailsTabsAdapter
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.util.extensions.*
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.originalOrDown
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsFragment : BaseFragment(R.layout.fragment_details, true),
    NavigationBarView.OnItemReselectedListener {

    private val args: DetailsFragmentArgs by navArgs()

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context?.isNightMode() == false) {
            activity?.clearLightStatusBar()
        }

        initAppBar()
        initTabLayout()

        viewModel.initMediaAdapter(args.model)

        viewModel.mediaAdapter.observe(viewLifecycleOwner) { model ->
            binding.data = model

            val glide = GlideApp.with(this)

            glide.load(model.coverImage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)

            glide.load(model.posterImage)
                .addTransform(RoundedCorners(8))
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

        }

        binding.ivThumbnail.setOnClickListener {
            viewModel.mediaAdapter.value?.let { mediaAdapter ->
                val title = mediaAdapter.title
                mediaAdapter.media.posterImage?.originalOrDown()?.let { imageUrl ->
                    openImageViewer(imageUrl, title, mediaAdapter.posterImage)
                }
            }
        }

        binding.ivCover.setOnClickListener {
            viewModel.mediaAdapter.value?.let { mediaAdapter ->
                val title = mediaAdapter.title
                mediaAdapter.media.coverImage?.originalOrDown()?.let { imageUrl ->
                    openImageViewer(imageUrl, title, mediaAdapter.coverImage)
                }
            }
        }

        viewModel.libraryEntry.observe(viewLifecycleOwner) { libraryEntry ->
            val isManga = libraryEntry?.manga != null
                    || viewModel.mediaAdapter.value?.isAnime() == false
            if (libraryEntry != null) {
                libraryEntry.status?.let { status ->
                    binding.btnManageLibrary.setText(status.getStringResId(!isManga))
                } ?: binding.btnManageLibrary.setText(R.string.library_action_add)
                binding.libraryEntry = LibraryEntryAdapter(LibraryEntryWrapper(libraryEntry, null))
            } else {
                // reset to defaults
                binding.btnManageLibrary.setText(R.string.library_action_add)
                binding.libraryEntry = null
            }
        }

        viewModel.favorite.observe(viewLifecycleOwner) { favorite ->
            val isFavorite = favorite != null
            updateFavoriteIcon(isFavorite)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressIndicator.isVisible = isLoading
        }

        binding.apply {
            content.initPaddingWindowInsetsListener(left = true, right = true)
            btnManageLibrary.setOnClickListener { showManageLibraryBottomSheet() }

            btnEditLibraryEntry.setOnClickListener { showEditLibraryEntryFragment() }
        }

        viewModel.errorResponseListener = { type ->
            val stringRes = when (type) {
                ErrorResponseType.LibraryUpdateFailed -> R.string.error_library_update_failed
            }
            Snackbar.make(binding.btnManageLibrary, stringRes, Snackbar.LENGTH_LONG)
                .apply { view.initMarginWindowInsetsListener(left = true, right = true) }
                .show()
        }

        setFragmentResultListener(ManageLibraryBottomSheet.STATUS_REQUEST_KEY) { _, bundle ->
            val libraryEntryStatus = bundle.get(ManageLibraryBottomSheet.BUNDLE_STATUS) as? Status
            libraryEntryStatus?.let { viewModel.updateLibraryEntryStatus(it) }
        }

        setFragmentResultListener(ManageLibraryBottomSheet.REMOVE_REQUEST_KEY) { _, bundle ->
            val shouldRemove = !bundle.getBoolean(ManageLibraryBottomSheet.BUNDLE_EXISTS_IN_LIBRARY)
            if (shouldRemove) {
                viewModel.removeLibraryEntry()
            }
        }
    }

    private fun initAppBar() {
        binding.apply {
            appBarLayout.addOnOffsetChangedListener(
                FadingToolbarOffsetListener(
                    requireActivity(),
                    toolbar
                )
            )

            toolbar.setNavigationOnClickListener { goBack() }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_media -> {
                        val url = viewModel.mediaAdapter.value?.let {
                            val prefix =
                                if (it.isAnime()) Kitsu.ANIME_URL_PREFIX else Kitsu.MANGA_URL_PREFIX
                            prefix + it.media.slug
                        }
                        if (url != null) {
                            startUrlShareIntent(url)
                        } else {
                            showSomethingWrongToast()
                        }
                    }
                    R.id.menu_favorite -> {
                        if (viewModel.isLoggedIn()) {
                            // update icon immediately before waiting for response
                            val willBeFavorite = viewModel.favorite.value == null
                            updateFavoriteIcon(willBeFavorite)
                            // send update to server
                            viewModel.toggleFavorite()
                        } else {
                            showLogInSnackbar()
                        }
                    }
                }
                true
            }

            val defaultTitleMarginStart = collapsingToolbar.expandedTitleMarginStart
            val defaultTitleMarginEnd = collapsingToolbar.expandedTitleMarginStart
            ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbar) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
                collapsingToolbar.expandedTitleMarginStart =
                    defaultTitleMarginStart + if (isRtl) insets.right else insets.left
                collapsingToolbar.expandedTitleMarginEnd =
                    defaultTitleMarginEnd + if (isRtl) insets.left else insets.right
                windowInsets
            }
            toolbar.initWindowInsetsListener(consume = false)
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.toolbar.menu.findItem(R.id.menu_favorite).apply {
            setIcon(
                if (isFavorite)
                    R.drawable.ic_favorite_24
                else
                    R.drawable.ic_favorite_border_24
            )
            setTitle(
                if (isFavorite)
                    R.string.action_remove_from_favorites
                else
                    R.string.action_add_to_favorites
            )
        }
    }

    private fun initTabLayout() {
        val detailsTabsAdapter = DetailsTabsAdapter(this)
        binding.viewPagerDetails.adapter = detailsTabsAdapter
        TabLayoutMediator(binding.tabLayoutDetails, binding.viewPagerDetails) { tab, position ->
            tab.text = "Tab $position"
        }.attach()
    }

    private fun showManageLibraryBottomSheet() {
        if (viewModel.isLoggedIn()) {
            viewModel.mediaAdapter.value?.let { mediaAdapter ->
                val sheetManageLibrary = ManageLibraryBottomSheet()
                val bundle = bundleOf(
                    ManageLibraryBottomSheet.BUNDLE_TITLE to mediaAdapter.title,
                    ManageLibraryBottomSheet.BUNDLE_IS_ANIME to mediaAdapter.isAnime(),
                    ManageLibraryBottomSheet.BUNDLE_EXISTS_IN_LIBRARY to (viewModel.libraryEntry.value != null)
                )
                sheetManageLibrary.arguments = bundle
                sheetManageLibrary.show(parentFragmentManager, ManageLibraryBottomSheet.TAG)
            }
        } else {
            showLogInSnackbar()
        }
    }

    private fun showEditLibraryEntryFragment() {
        if (!viewModel.isLoggedIn()) return
        val entryId = viewModel.libraryEntry.value?.id ?: return
        val action =
            DetailsFragmentDirections.actionDetailsFragmentToLibraryEditEntryFragment(entryId)
        findNavController().navigateSafe(R.id.details_fragment, action)
    }

    private fun showLogInSnackbar() {
        Snackbar.make(
            binding.btnManageLibrary,
            R.string.info_log_in_required,
            Snackbar.LENGTH_LONG
        ).apply {
            view.initMarginWindowInsetsListener(left = true, right = true)
            setAction(R.string.action_log_in) {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }.show()
    }

    private fun openImageViewer(imageUrl: String, title: String?, thumbnailUrl: String?) {
        val action = DetailsFragmentDirections.actionDetailsFragmentToPhotoViewActivity(
            imageUrl,
            title,
            thumbnailUrl
        )
        findNavController().navigate(action)
    }

    private fun goBack() {
        findNavController().navigateUp()
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.nsvContent.canScrollVertically(-1)) {
            binding.nsvContent.smoothScrollTo(0, 0)
            binding.appBarLayout.setExpanded(true)
        } else {
            goBack()
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
            activity?.setLightStatusBar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}