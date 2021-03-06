package io.github.drumber.kitsune.ui.details

import android.content.Intent
import android.net.Uri
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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.model.MediaSelector
import io.github.drumber.kitsune.data.model.MediaType
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.library.LibraryEntryAdapter
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.model.library.getStringResId
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder.TagData
import io.github.drumber.kitsune.ui.adapter.StreamingLinkAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.library.RatingBottomSheet
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.ui.widget.chart.BarChartStyle
import io.github.drumber.kitsune.ui.widget.chart.BarChartStyle.applyStyle
import io.github.drumber.kitsune.ui.widget.chart.StepAxisValueFormatter
import io.github.drumber.kitsune.util.extensions.*
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.originalOrDown
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList

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

        viewModel.initMediaAdapter(args.model)

        viewModel.mediaAdapter.observe(viewLifecycleOwner) { model ->
            binding.data = model
            showCategoryChips(model)
            showFranchise(model)
            showStreamingLinks(model)
            showRatingChart(model)

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

        viewModel.libraryEntry.observe(viewLifecycleOwner) {
            it?.let { libraryEntry ->
                libraryEntry.status?.let { status ->
                    binding.btnManageLibrary.setText(status.getStringResId())
                } ?: binding.btnManageLibrary.setText(R.string.library_action_add)
                binding.libraryEntry = LibraryEntryAdapter(LibraryEntryWrapper(libraryEntry, null))
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
            btnMediaUnits.setOnClickListener {
                val media = args.model.media
                val libraryEntry = viewModel.libraryEntry.value
                val action = DetailsFragmentDirections.actionDetailsFragmentToEpisodesFragment(
                    media,
                    libraryEntry?.id
                )
                findNavController().navigate(action)
            }
            btnCharacters.setOnClickListener {
                val media = args.model.media
                val action = DetailsFragmentDirections.actionDetailsFragmentToCharactersFragment(
                    media.id,
                    media is Anime
                )
                findNavController().navigate(action)
            }
            btnRating.setOnClickListener { showRatingBottomSheet() }
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

        setFragmentResultListener(RatingBottomSheet.RATING_REQUEST_KEY) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateLibraryEntryRating(rating)
            }
        }

        setFragmentResultListener(RatingBottomSheet.REMOVE_RATING_REQUEST_KEY) { _, _ ->
            viewModel.updateLibraryEntryRating(null)
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

    private fun showCategoryChips(mediaAdapter: MediaAdapter) {
        if (!mediaAdapter.categories.isNullOrEmpty()) {
            binding.chipGroupCategories.removeAllViews()

            mediaAdapter.categories.orEmpty()
                .sortedBy { it.title }
                .forEach { category ->
                    val chip = Chip(requireContext())
                    chip.text = category.title
                    chip.setOnClickListener {
                        onCategoryChipClicked(category, mediaAdapter)
                    }
                    binding.chipGroupCategories.addView(chip)
                }
        }
    }

    private fun onCategoryChipClicked(category: Category, mediaAdapter: MediaAdapter) {
        val categorySlug = category.slug ?: return
        val title = category.title ?: getString(R.string.no_information)

        val mediaSelector = MediaSelector(
            if (mediaAdapter.isAnime()) MediaType.Anime else MediaType.Manga,
            Filter()
                .filter("categories", categorySlug)
                .sort(SortFilter.POPULARITY_DESC.queryParam)
        )

        val action =
            DetailsFragmentDirections.actionDetailsFragmentToMediaListFragment(mediaSelector, title)
        findNavController().navigate(action)
    }

    private fun showFranchise(mediaAdapter: MediaAdapter) {
        val data = mediaAdapter.media.mediaRelationships?.sortedBy {
            it.role?.ordinal
        }?.mapNotNull {
            it.media?.let { media -> MediaAdapter.fromMedia(media, it.role) }
        } ?: emptyList()

        if (binding.rvFranchise.adapter !is MediaRecyclerViewAdapter) {
            val glide = GlideApp.with(this)
            val adapter = MediaRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide,
                TagData.RelationshipRole
            ) { media ->
                onFranchiseItemClicked(media)
            }
            adapter.overrideItemSize = MediaItemSize.SMALL
            binding.rvFranchise.adapter = adapter
        } else {
            val adapter = binding.rvFranchise.adapter as MediaRecyclerViewAdapter
            adapter.dataSet.addAll(0, data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onFranchiseItemClicked(mediaAdapter: MediaAdapter) {
        val action = DetailsFragmentDirections.actionDetailsFragmentSelf(mediaAdapter)
        findNavController().navigateSafe(R.id.details_fragment, action)
    }

    private fun showStreamingLinks(mediaAdapter: MediaAdapter) {
        val data = (mediaAdapter.media as? Anime)?.streamingLinks ?: emptyList()

        if (binding.rvStreamer.adapter !is StreamingLinkAdapter) {
            val glide = GlideApp.with(this)
            val adapter = StreamingLinkAdapter(CopyOnWriteArrayList(data), glide) { streamingLink ->
                streamingLink.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
            binding.rvStreamer.adapter = adapter
        } else {
            val adapter = binding.rvStreamer.adapter as StreamingLinkAdapter
            adapter.dataSet.addAll(0, data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showRatingChart(mediaAdapter: MediaAdapter) {
        val ratings = mediaAdapter.media.ratingFrequencies ?: return


        val displayWidth = resources.displayMetrics.widthPixels
        // full chart will show ratings 1-10 with step size 0.5; reduced chart with step size 1
        val isFullChart =
            displayWidth >= resources.getDimensionPixelSize(R.dimen.details_rating_chart_full_threshold)

        val ratingList = with(ratings) {
            val list = listOf(
                r2,
                r3,
                r4,
                r5,
                r6,
                r7,
                r8,
                r9,
                r10,
                r11,
                r12,
                r13,
                r14,
                r15,
                r16,
                r17,
                r18,
                r19,
                r20
            )

            if (isFullChart) {
                list
            } else {
                var previous = 0
                list.mapIndexedNotNull { index, s ->
                    if (index % 2 == 0) {
                        (previous + (s?.toInt() ?: 0)).toString()
                    } else {
                        previous = s?.toInt() ?: 0
                        null
                    }
                }
            }
        }

        val chartEntries = ratingList.mapIndexed { index, s ->
            BarEntry(index.toFloat(), s?.toFloat() ?: 0f)
        }

        val dataSet = BarDataSet(chartEntries, "Ratings")
        val chartColorArray = BarChartStyle
            .getColorArray(requireContext(), R.array.ratings_chart_colors)
            .filterIndexed { index, _ ->
                isFullChart || index % 2 == 0
            }
        dataSet.applyStyle(requireContext(), chartColorArray)

        val barData = BarData(dataSet)
        barData.applyStyle(requireContext())

        binding.chartRatings.apply {
            data = barData
            applyStyle(requireContext())
            setFitBars(true)
            xAxis.valueFormatter = StepAxisValueFormatter(1f, if (isFullChart) 0.5f else 1f)
            xAxis.labelCount = if (isFullChart) 19 else 10
            invalidate()
        }
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

    private fun showRatingBottomSheet() {
        val libraryEntry = viewModel.libraryEntry.value ?: return
        val mediaAdapter = viewModel.mediaAdapter.value ?: return

        val sheetLibraryRating = RatingBottomSheet()
        val bundle = bundleOf(
            RatingBottomSheet.BUNDLE_TITLE to mediaAdapter.title,
            RatingBottomSheet.BUNDLE_RATING to libraryEntry.ratingTwenty
        )
        sheetLibraryRating.arguments = bundle
        sheetLibraryRating.show(parentFragmentManager, RatingBottomSheet.TAG)
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