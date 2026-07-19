package io.github.drumber.kitsune.ui.details

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMedia
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.getStringResId
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.databinding.DialogComposeReactionBinding
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.ui.adapter.MediaReactionPreviewAdapter
import io.github.drumber.kitsune.ui.adapter.MediaRelationshipRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.StreamingLinkAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.AddNewLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.DeleteLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.util.extensions.getColor
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.logW
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.showSnackbarOnFailure
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList

class DetailsFragment : BaseFragment(R.layout.fragment_details, true),
    NavigationBarView.OnItemReselectedListener {

    private val args: DetailsFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentDetailsBinding::bind)

    private val viewModel: DetailsViewModel by viewModel()

    private var reactionsAdapter: MediaReactionPreviewAdapter? = null

    private val ratingChartSection by lazy {
        DetailsRatingChartSection(binding, requireContext())
    }

    private val titlesSection by lazy {
        DetailsTitlesSection(
            binding = binding,
            layoutInflater = layoutInflater,
            resolveColor = { attr -> requireActivity().theme.getColor(attr) },
            isExpanded = { viewModel.areAllTileLanguagesShown },
            setExpanded = { viewModel.areAllTileLanguagesShown = it },
            currentTitles = { viewModel.mediaModel.value?.titles }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.material_motion_duration_short_2).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(SurfaceColors.SURFACE_0.getColor(requireContext()))
        }
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        if (args.media != null) {
            viewModel.initMediaModel(args.media!!.toMedia())
        } else if (!args.type.isNullOrBlank() && !args.slug.isNullOrBlank()) {
            val isAnime = when (args.type!!.lowercase()) {
                "anime" -> true
                "manga" -> false
                else -> null
            }

            if (isAnime == null) {
                logW("Unknown media type '${args.type}'.")
                showSomethingWrongToast()
                goBack()
            } else {
                viewModel.initFromDeepLink(isAnime, args.slug!!)
            }
        } else {
            logW("DetailsFragment opened without media bundle or invalid deeplink parameters.")
            showSomethingWrongToast()
            goBack()
        }

        initAppBar()

        setupReactions()

        viewModel.mediaModel.observe(viewLifecycleOwner) { model ->
            binding.data = model
            titlesSection.updateTitlesInDetailsTable(model.titles)
            showCategoryChips(model)
            showFranchise(model)
            showStreamingLinks(model)
            ratingChartSection.showRatingChart(model)

            val glide = Glide.with(this)

            glide.load(model.coverImageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)

            glide.load(model.posterImageUrl)
                .addTransform(RoundedCorners(8.toPx()))
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

        }

        binding.ivThumbnail.setOnClickListener {
            viewModel.mediaModel.value?.let { media ->
                val title = media.title
                media.posterImage?.originalOrDown()?.let { imageUrl ->
                    openPhotoViewActivity(
                        imageUrl,
                        title,
                        media.posterImageUrl,
                        binding.ivThumbnail
                    )
                }
            }
        }

        binding.ivCover.setOnClickListener {
            viewModel.mediaModel.value?.let { media ->
                val title = media.title
                media.coverImage?.originalOrDown()?.let { imageUrl ->
                    openPhotoViewActivity(imageUrl, title, media.coverImageUrl, binding.ivCover)
                }
            }
        }

        viewModel.libraryEntryWrapper.observe(viewLifecycleOwner) { libraryEntryWithModification ->
            val isManga = libraryEntryWithModification?.libraryEntry?.media is Manga
                    || viewModel.mediaModel.value is Manga
            if (libraryEntryWithModification != null) {
                libraryEntryWithModification.status?.let { status ->
                    binding.btnManageLibrary.setText(status.getStringResId(!isManga))
                } ?: binding.btnManageLibrary.setText(R.string.library_action_add)
                binding.libraryEntry = libraryEntryWithModification
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
            content.initPaddingWindowInsetsListener(left = true, right = true, bottom = true)
            btnManageLibrary.setOnClickListener { showManageLibraryBottomSheet() }
            btnMediaUnits.setOnClickListener {
                val media = viewModel.mediaModel.value ?: return@setOnClickListener
                val action = DetailsFragmentDirections.actionDetailsFragmentToEpisodesFragment(
                    media.toMediaDto()
                )
                findNavController().navigate(action)
            }
            btnCharacters.setOnClickListener {
                val media = viewModel.mediaModel.value ?: return@setOnClickListener
                val action = DetailsFragmentDirections.actionDetailsFragmentToCharactersFragment(
                    media.id,
                    media is Anime
                )
                findNavController().navigate(action)
            }
            btnMediaFeed.setOnClickListener {
                val media = viewModel.mediaModel.value ?: return@setOnClickListener
                val action = DetailsFragmentDirections.actionDetailsFragmentToMediaFeedFragment(
                    media.id,
                    media is Anime
                )
                findNavController().navigate(action)
            }
            btnReactionsSeeAll.setOnClickListener {
                val media = viewModel.mediaModel.value ?: return@setOnClickListener
                val action = DetailsFragmentDirections.actionDetailsFragmentToReactionsFragment(
                    media.id,
                    media is Anime
                )
                findNavController().navigate(action)
            }

            btnEditLibraryEntry.setOnClickListener { showEditLibraryEntryFragment() }

            btnRatingTypeMenu.setOnClickListener { v ->
                ratingChartSection.showRatingTypeMenu(v, viewModel.mediaModel.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.libraryChangeResultFlow.collectLatest {
                when (it) {
                    is LibraryUpdateResult -> it.result.showSnackbarOnFailure(binding.btnManageLibrary)
                    is AddNewLibraryEntryFailed -> showSnackbar(
                        binding.btnManageLibrary,
                        R.string.error_library_add_failed
                    )

                    is DeleteLibraryEntryFailed -> showSnackbar(
                        binding.btnManageLibrary,
                        R.string.error_library_delete_failed
                    )
                }
            }
        }

        setFragmentResultListener(ManageLibraryBottomSheet.STATUS_REQUEST_KEY) { _, bundle ->
            val libraryEntryStatus =
                BundleCompat.getSerializable(
                    bundle,
                    ManageLibraryBottomSheet.BUNDLE_STATUS,
                    LibraryStatus::class.java
                )
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
            toolbar.setNavigationOnClickListener { goBack() }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_media -> {
                        val url = viewModel.mediaModel.value?.let {
                            val prefix =
                                if (it is Anime) Kitsu.ANIME_URL_PREFIX else Kitsu.MANGA_URL_PREFIX
                            prefix + it.slug
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
                            updateFavoriteIcon(willBeFavorite, true)
                            // send update to server
                            viewModel.toggleFavorite()
                        } else {
                            showLogInSnackbar()
                        }
                    }

                    R.id.menu_open_external -> {
                        viewModel.loadMappingsIfNotAlreadyLoaded()
                        val mappingsBottomSheet = MediaMappingsBottomSheet()
                        mappingsBottomSheet.show(childFragmentManager, MediaMappingsBottomSheet.TAG)
                    }
                }
                true
            }

            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(consume = false)
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean, isUserAction: Boolean = false) {
        val menuItem = binding.toolbar.menu.findItem(R.id.menu_favorite)

        if (isFavorite && isUserAction) {
            AnimatedVectorDrawableCompat.create(
                requireContext(),
                R.drawable.animated_favorite
            )?.apply {
                menuItem.icon = this
                registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        menuItem.setIcon(R.drawable.ic_favorite_24)
                    }
                })
                start()
            }
        } else if (menuItem.icon !is AnimatedVectorDrawableCompat || !isFavorite) {
            menuItem.setIcon(
                if (isFavorite) R.drawable.ic_favorite_24
                else R.drawable.ic_favorite_border_24
            )
        }

        menuItem.setTitle(
            if (isFavorite)
                R.string.action_remove_from_favorites
            else
                R.string.action_add_to_favorites
        )
    }

    private fun showCategoryChips(media: Media) {
        if (!media.categories.isNullOrEmpty()) {
            binding.chipGroupCategories.removeAllViews()

            media.categories.orEmpty()
                .sortedBy { it.title }
                .forEach { category ->
                    val chip = Chip(requireContext())
                    chip.text = category.title
                    chip.setOnClickListener {
                        onCategoryChipClicked(category, media)
                    }
                    binding.chipGroupCategories.addView(chip)
                }
        }
    }

    private fun onCategoryChipClicked(category: Category, media: Media) {
        val categorySlug = category.slug ?: return
        val title = category.title ?: getString(R.string.no_information)

        val mediaSelector = MediaSelector(
            if (media is Anime) MediaType.Anime else MediaType.Manga,
            Filter()
                .filter("categories", categorySlug)
                .sort(SortFilter.POPULARITY_DESC.queryParam)
                .options
        )

        val action =
            DetailsFragmentDirections.actionDetailsFragmentToMediaListFragment(mediaSelector, title)
        findNavController().navigate(action)
    }

    private fun showFranchise(media: Media) {
        val data = media.mediaRelationships?.sortedBy { it.role?.ordinal } ?: emptyList()

        if (binding.rvFranchise.adapter !is MediaRelationshipRecyclerViewAdapter) {
            val glide = Glide.with(this)
            val adapter = MediaRelationshipRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide
            ) { view, clickedMedia ->
                clickedMedia.media?.let { onFranchiseItemClicked(view, it) }
            }
            binding.rvFranchise.adapter = adapter
        } else {
            val adapter = binding.rvFranchise.adapter as MediaRelationshipRecyclerViewAdapter
            adapter.dataSet.addAll(0, data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onFranchiseItemClicked(view: View, media: Media) {
        val action = DetailsFragmentDirections.actionDetailsFragmentSelf(media.toMediaDto())
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.details_fragment, action, extras)
    }

    private fun setupReactions() {
        val adapter = MediaReactionPreviewAdapter(Glide.with(this)) { reaction ->
            viewModel.upvoteReaction(reaction)
        }
        reactionsAdapter = adapter
        binding.rvReactions.adapter = adapter

        binding.btnAddReaction.setOnClickListener { showComposeReactionDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reactions.collectLatest { reactions ->
                // The section stays visible even with no reactions so the add button is always
                // reachable; only the list itself collapses when empty.
                binding.layoutReactions.isVisible = true
                binding.rvReactions.isVisible = reactions.isNotEmpty()
                adapter.submitList(reactions)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reactionUpvoteEvents.collectLatest { event ->
                when (event) {
                    ReactionUpvoteEvent.LoginRequired ->
                        showSnackbar(binding.root, R.string.reactions_upvote_login_required)

                    is ReactionUpvoteEvent.Success ->
                        reactionsAdapter?.markUpvoted(event.reactionId, event.newCount)

                    ReactionUpvoteEvent.Failed ->
                        showSnackbar(binding.root, R.string.reactions_upvote_failed)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reactionEditEvents.collectLatest { event ->
                when (event) {
                    ReactionEditEvent.LoginRequired ->
                        showSnackbar(binding.root, R.string.reaction_login_required)

                    ReactionEditEvent.AddToLibraryRequired ->
                        showSnackbar(binding.root, R.string.reaction_add_to_library_required)

                    ReactionEditEvent.Created ->
                        showSnackbar(binding.root, R.string.reaction_posted)

                    ReactionEditEvent.Failed ->
                        showSnackbar(binding.root, R.string.action_failed)
                }
            }
        }
    }

    private fun showComposeReactionDialog() {
        val dialogBinding = DialogComposeReactionBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reaction_compose_title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.reaction_compose_action_post) { _, _ ->
                val text = dialogBinding.etReaction.text?.toString().orEmpty().trim()
                if (text.isNotEmpty()) viewModel.createReaction(text)
            }
            .show()
    }

    private fun showStreamingLinks(media: Media) {
        val data = (media as? Anime)?.streamingLinks ?: emptyList()
        if (binding.rvStreamer.adapter !is StreamingLinkAdapter) {
            val glide = Glide.with(this)
            val adapter =
                StreamingLinkAdapter(glide) { _, streamingLink ->
                    streamingLink.url?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }
            binding.rvStreamer.adapter = adapter
            adapter.submitList(data)
        } else {
            val adapter = binding.rvStreamer.adapter as StreamingLinkAdapter
            adapter.submitList(data)
        }
    }

    private fun showManageLibraryBottomSheet() {
        if (viewModel.isLoggedIn()) {
            viewModel.mediaModel.value?.let { mediaAdapter ->
                val sheetManageLibrary = ManageLibraryBottomSheet()
                val bundle = bundleOf(
                    ManageLibraryBottomSheet.BUNDLE_TITLE to mediaAdapter.title,
                    ManageLibraryBottomSheet.BUNDLE_IS_ANIME to (mediaAdapter is Anime),
                    ManageLibraryBottomSheet.BUNDLE_EXISTS_IN_LIBRARY to (viewModel.libraryEntryWrapper.value != null)
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
        val entryId = viewModel.libraryEntryWrapper.value?.libraryEntry?.id ?: return
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
            view.initMarginWindowInsetsListener(left = true, right = true, bottom = true)
            setAction(R.string.action_log_in) {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }.show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        reactionsAdapter = null
    }

}