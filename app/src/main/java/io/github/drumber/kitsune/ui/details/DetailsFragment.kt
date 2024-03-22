package io.github.drumber.kitsune.ui.details

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.children
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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.chip.Chip
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.databinding.ItemDetailsInfoRowBinding
import io.github.drumber.kitsune.domain.model.MediaSelector
import io.github.drumber.kitsune.domain.model.MediaType
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.common.media.Titles
import io.github.drumber.kitsune.domain.model.common.media.en
import io.github.drumber.kitsune.domain.model.common.media.withoutCommonTitles
import io.github.drumber.kitsune.domain.model.infrastructure.media.Anime
import io.github.drumber.kitsune.domain.model.infrastructure.media.category.Category
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryAdapter
import io.github.drumber.kitsune.domain.model.ui.library.getStringResId
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.domain.model.ui.media.originalOrDown
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder.TagData
import io.github.drumber.kitsune.ui.adapter.StreamingLinkAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.AddNewLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.DeleteLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.ui.widget.chart.BarChartStyle
import io.github.drumber.kitsune.ui.widget.chart.BarChartStyle.applyStyle
import io.github.drumber.kitsune.ui.widget.chart.StepAxisValueFormatter
import io.github.drumber.kitsune.util.DataUtil.mapLanguageCodesToDisplayName
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList

class DetailsFragment : BaseFragment(R.layout.fragment_details, true),
    NavigationBarView.OnItemReselectedListener {

    private val args: DetailsFragmentArgs by navArgs()

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModel()

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
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        if (args.model != null) {
            viewModel.initMediaAdapter(args.model!!)
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
            logW("DetailsFragment opened without media adapter or invalid deeplink parameters.")
            showSomethingWrongToast()
            goBack()
        }

        initAppBar()

        viewModel.mediaAdapter.observe(viewLifecycleOwner) { model ->
            binding.data = model
            updateTitlesInDetailsTable(model.titles)
            showCategoryChips(model)
            showFranchise(model)
            showStreamingLinks(model)
            showRatingChart(model)

            val glide = Glide.with(this)

            glide.load(model.coverImage)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)

            glide.load(model.posterImage)
                .addTransform(RoundedCorners(8.toPx()))
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

        }

        binding.ivThumbnail.setOnClickListener {
            viewModel.mediaAdapter.value?.let { mediaAdapter ->
                val title = mediaAdapter.title
                mediaAdapter.media.posterImage?.originalOrDown()?.let { imageUrl ->
                    openPhotoViewActivity(
                        imageUrl,
                        title,
                        mediaAdapter.posterImage,
                        binding.ivThumbnail
                    )
                }
            }
        }

        binding.ivCover.setOnClickListener {
            viewModel.mediaAdapter.value?.let { mediaAdapter ->
                val title = mediaAdapter.title
                mediaAdapter.media.coverImage?.originalOrDown()?.let { imageUrl ->
                    openPhotoViewActivity(imageUrl, title, mediaAdapter.coverImage, binding.ivCover)
                }
            }
        }

        viewModel.libraryEntryWrapper.observe(viewLifecycleOwner) { libraryEntryWrapper ->
            val isManga = libraryEntryWrapper?.libraryEntry?.manga != null
                    || viewModel.mediaAdapter.value?.isAnime() == false
            if (libraryEntryWrapper != null) {
                libraryEntryWrapper.status?.let { status ->
                    binding.btnManageLibrary.setText(status.getStringResId(!isManga))
                } ?: binding.btnManageLibrary.setText(R.string.library_action_add)
                binding.libraryEntry = LibraryEntryAdapter(libraryEntryWrapper)
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
            btnMediaUnits.setOnClickListener {
                val media = viewModel.mediaAdapter.value?.media ?: return@setOnClickListener
                val libraryEntry = viewModel.libraryEntryWrapper.value?.libraryEntry
                val action = DetailsFragmentDirections.actionDetailsFragmentToEpisodesFragment(
                    media,
                    libraryEntry?.id
                )
                findNavController().navigate(action)
            }
            btnCharacters.setOnClickListener {
                val media = viewModel.mediaAdapter.value ?: return@setOnClickListener
                val action = DetailsFragmentDirections.actionDetailsFragmentToCharactersFragment(
                    media.id,
                    media.isAnime()
                )
                findNavController().navigate(action)
            }

            btnEditLibraryEntry.setOnClickListener { showEditLibraryEntryFragment() }
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
                bundle.get(ManageLibraryBottomSheet.BUNDLE_STATUS) as? LibraryStatus
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
                            updateFavoriteIcon(willBeFavorite, true)
                            // send update to server
                            viewModel.toggleFavorite()
                        } else {
                            showLogInSnackbar()
                        }
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

    private fun updateTitlesInDetailsTable(titles: Titles?) {
        val identifierTag = "dynamic_title"
        val tableLayout = binding.sectionDetailsInfo.tableLayout
        // remove any previous added titles
        tableLayout.apply {
            children.filter { it.tag == identifierTag }.toList().forEach { removeView(it) }
        }

        // map language codes and sort them
        val sortedTitles = titles?.withoutCommonTitles()
            ?.filterValues { !it.isNullOrBlank() }
            ?.filterNot { it.key == "en_us" && it.value == titles.en }
            ?.mapLanguageCodesToDisplayName()
            ?.toList()
            ?.sortedByDescending { it.first }

        if (sortedTitles.isNullOrEmpty()) return

        val maxShownTitles = 3
        val shouldLimitShownTitles = sortedTitles.size > maxShownTitles &&
                !viewModel.areAllTileLanguagesShown
        val rowIndex = tableLayout.indexOfChild(binding.sectionDetailsInfo.synonymsRowLayout.root)
            .coerceAtLeast(0)
        // add a row for each title
        sortedTitles
            .takeLast(if (shouldLimitShownTitles) maxShownTitles else Int.MAX_VALUE)
            .forEach { (language, title) ->
                val rowBinding = ItemDetailsInfoRowBinding.inflate(layoutInflater)
                rowBinding.title = language
                rowBinding.value = title
                rowBinding.root.tag = identifierTag
                tableLayout.addView(rowBinding.root, rowIndex)
            }

        // add 'show more' text to table
        if (sortedTitles.size > maxShownTitles) {
            val showMoreRow = createShowMoreTitlesRow()
            showMoreRow.tag = identifierTag
            val viewIndex =
                tableLayout.indexOfChild(binding.sectionDetailsInfo.synonymsRowLayout.root)
                    .coerceAtLeast(0)
            tableLayout.addView(showMoreRow, viewIndex)
        }
    }

    private fun createShowMoreTitlesRow(): View {
        val rowBinding = ItemDetailsInfoRowBinding.inflate(layoutInflater)
        val text = getString(
            if (viewModel.areAllTileLanguagesShown)
                R.string.action_show_less
            else
                R.string.action_show_more
        )
        rowBinding.title = SpannableString(text).apply {
            setSpan(
                ForegroundColorSpan(requireActivity().theme.getColor(R.attr.colorPrimary)),
                0,
                text.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            setSpan(UnderlineSpan(), 0, text.length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        rowBinding.tvTitle.setOnClickListener {
            viewModel.areAllTileLanguagesShown = !viewModel.areAllTileLanguagesShown
            updateTitlesInDetailsTable(viewModel.mediaAdapter.value?.titles)
        }
        return rowBinding.root
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
            val glide = Glide.with(this)
            val adapter = MediaRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide,
                TagData.RelationshipRole
            ) { view, media ->
                onFranchiseItemClicked(view, media)
            }
            adapter.overrideItemSize = MediaItemSize.SMALL
            binding.rvFranchise.adapter = adapter
        } else {
            val adapter = binding.rvFranchise.adapter as MediaRecyclerViewAdapter
            adapter.dataSet.addAll(0, data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onFranchiseItemClicked(view: View, mediaAdapter: MediaAdapter) {
        val action = DetailsFragmentDirections.actionDetailsFragmentSelf(mediaAdapter)
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.details_fragment, action, extras)
    }

    private fun showStreamingLinks(mediaAdapter: MediaAdapter) {
        val data = (mediaAdapter.media as? Anime)?.streamingLinks ?: emptyList()

        if (binding.rvStreamer.adapter !is StreamingLinkAdapter) {
            val glide = Glide.with(this)
            val adapter =
                StreamingLinkAdapter(CopyOnWriteArrayList(data), glide) { _, streamingLink ->
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
        // full chart shows advanced ratings (1-10); reduced chart with shows regular rating (0.5-5)
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
            xAxis.valueFormatter = StepAxisValueFormatter(if (isFullChart) 1f else 0.5f, 0.5f)
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
            view.initMarginWindowInsetsListener(left = true, right = true)
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
        _binding = null
    }

}