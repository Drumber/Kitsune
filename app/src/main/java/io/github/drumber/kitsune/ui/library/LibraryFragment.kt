package io.github.drumber.kitsune.ui.library

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import by.kirich1409.viewbindingdelegate.viewBinding
import com.algolia.instantsearch.core.searcher.Debouncer
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryUiModel
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.databinding.FragmentLibraryBinding
import io.github.drumber.kitsune.ui.adapter.paging.LibraryEntriesAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.component.ResponsiveGridLayoutManager
import io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibrarySynchronizationResult
import io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.setStatusBarColorRes
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbarOnAnyFailure
import io.github.drumber.kitsune.util.ui.showSnackbarOnFailure
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : BaseFragment(R.layout.fragment_library, true),
    MenuProvider,
    LibraryEntriesAdapter.LibraryEntryActionListener,
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentLibraryBinding by viewBinding()

    private val viewModel: LibraryViewModel by viewModel()

    private var isStatusBarTransparent = false

    private var offlineLibraryModificationsAmount = 0
    private lateinit var offlineLibraryUpdateBadge: BadgeDrawable

    private val searchDebouncer by lazy { Debouncer(300L) }

    private val autoSyncDebouncer by lazy { Debouncer(5000L) }

    companion object {
        const val RESULT_KEY_RATING = "library_rating_result_key"
        const val RESULT_KEY_REMOVE_RATING = "library_remove_rating_result_key"
        const val RESULT_KEY_EDIT_ENTRY_UPDATED = "library_edit_entry_updated"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offlineLibraryUpdateBadge = BadgeDrawable.create(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        binding.apply {
            toolbar.initWindowInsetsListener(consume = false)
            scrollViewFilter.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
                val shouldSetStatusBarTransparent = verticalOffset >= -30
                if (isStatusBarTransparent == shouldSetStatusBarTransparent)
                    return@addOnOffsetChangedListener
                setStatusBarTransparent(shouldSetStatusBarTransparent)
            }

            swipeRefreshLayout.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            layoutNotLoggedIn.initPaddingWindowInsetsListener(
                left = true,
                top = true,
                right = true,
                consume = false
            )

            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }
        val initialToolbarScrollFlags =
            (binding.toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.localUser.collectLatest { user ->
                    val isLoggedIn = user != null
                    binding.apply {
                        setMenuVisibility(isLoggedIn)
                        rvLibraryEntries.isVisible = isLoggedIn
                        nsvNotLoggedIn.isVisible = !isLoggedIn
                        scrollViewFilter.isVisible = isLoggedIn
                        // disable toolbar scrolling if library is not shown (not logged in)
                        (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                            if (isLoggedIn) {
                                initialToolbarScrollFlags
                            } else {
                                AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
                            }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.libraryChangeResultFlow.collectLatest {
                when (it) {
                    is LibraryUpdateResult -> it.result.showSnackbarOnFailure(binding.rvLibraryEntries)
                    is LibrarySynchronizationResult -> it.results.showSnackbarOnAnyFailure(binding.rvLibraryEntries)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .map { it.isLibraryUpdateOperationInProgress }
                .distinctUntilChanged()
                .collectLatest {
                    binding.progressIndicator.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
        }

        setFragmentResultListener(RESULT_KEY_RATING) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateRating(rating)
            }
        }

        setFragmentResultListener(RESULT_KEY_REMOVE_RATING) { _, _ ->
            viewModel.updateRating(null)
        }

        setFragmentResultListener(RESULT_KEY_EDIT_ENTRY_UPDATED) { _, _ ->
            viewModel.triggerAdapterUpdate()
        }

        initFilterChips()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        setStatusBarTransparent(isStatusBarTransparent)
    }

    private fun initFilterChips() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .map { it.filter.kind }
                .distinctUntilChanged()
                .collectLatest { kind ->
                    binding.chipMediaKind.setText(
                        when (kind) {
                            LibraryEntryKind.Anime -> R.string.anime
                            LibraryEntryKind.Manga -> R.string.manga
                            else -> R.string.library_kind_all
                        }
                    )

                    binding.chipCurrent.setText(
                        if (kind == LibraryEntryKind.Manga) R.string.library_status_reading
                        else R.string.library_status_watching
                    )
                    binding.chipPlanned.setText(
                        if (kind == LibraryEntryKind.Manga) R.string.library_status_planned_manga
                        else R.string.library_status_planned
                    )
                }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .map { it.filter.libraryStatus }
                .distinctUntilChanged()
                .collectLatest { status ->
                    binding.apply {
                        chipCurrent.isChecked = status.contains(LibraryStatus.Current)
                        chipPlanned.isChecked = status.contains(LibraryStatus.Planned)
                        chipCompleted.isChecked = status.contains(LibraryStatus.Completed)
                        chipOnHold.isChecked = status.contains(LibraryStatus.OnHold)
                        chipDropped.isChecked = status.contains(LibraryStatus.Dropped)
                    }
                }
        }

        binding.apply {
            chipMediaKind.setOnClickListener { showMediaSelectorDialog() }
            chipCurrent.initStatusClickListener(LibraryStatus.Current)
            chipPlanned.initStatusClickListener(LibraryStatus.Planned)
            chipCompleted.initStatusClickListener(LibraryStatus.Completed)
            chipOnHold.initStatusClickListener(LibraryStatus.OnHold)
            chipDropped.initStatusClickListener(LibraryStatus.Dropped)
        }
    }

    private fun Chip.initStatusClickListener(status: LibraryStatus) {
        setOnClickListener {
            val statusList = viewModel.state.value.filter.libraryStatus.toMutableList()
            if (statusList.contains(status)) {
                statusList.remove(status)
            } else {
                statusList.add(status)
            }
            viewModel.setLibraryEntryStatus(statusList)
        }
    }

    private fun showMediaSelectorDialog() {
        val items = listOf(R.string.library_kind_all, R.string.anime, R.string.manga)
            .map { getString(it) }.toTypedArray()
        val prevSelected = viewModel.state.value.filter.kind.ordinal
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_media_type)
            .setSingleChoiceItems(items, prevSelected) { dialog, which ->
                if (which != prevSelected) {
                    val kind = LibraryEntryKind.entries[which]
                    viewModel.setLibraryEntryKind(kind)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun initRecyclerView() {
        val glide = Glide.with(this)
        val adapter = LibraryEntriesAdapter(glide, this)

        var lastLoadState: CombinedLoadStates? = null

        adapter.addLoadStateListener { state ->
            lastLoadState = state
            if (view?.parent != null) {
                val isSearching = viewModel.state.value.filter.searchQuery.isNotBlank()
                val isNotLoading = when {
                    adapter.itemCount < 1 -> state.refresh is LoadState.NotLoading

                    isSearching -> state.source.refresh is LoadState.NotLoading

                    else -> state.mediator?.refresh !is LoadState.Loading
                            || state.source.refresh is LoadState.NotLoading
                }

                binding.apply {
                    rvLibraryEntries.isVisible = isNotLoading
                    layoutLoading.apply {
                        root.isVisible = !isNotLoading
                        progressBar.isVisible = state.refresh is LoadState.Loading
                        btnRetry.isVisible = state.mediator?.refresh is LoadState.Error
                        tvError.isVisible = state.mediator?.refresh is LoadState.Error
                    }

                    if (state.refresh is LoadState.NotLoading
                        && state.append.endOfPaginationReached
                        && adapter.itemCount < 1
                    ) {
                        layoutLoading.root.isVisible = true
                        layoutLoading.tvNoData.isVisible = true
                        rvLibraryEntries.isVisible = false
                    } else {
                        layoutLoading.tvNoData.isVisible = false
                    }

                    swipeRefreshLayout.isRefreshing =
                        swipeRefreshLayout.isRefreshing && state.source.refresh is LoadState.Loading
                }
            }
        }

        binding.rvLibraryEntries.apply {
            this.adapter = adapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(adapter),
                footer = ResourceLoadStateAdapter(adapter)
            )
            layoutManager = ResponsiveGridLayoutManager(context, 350.toPx(), 1).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter.getItemViewType(position) == R.layout.item_library_entry) {
                            1
                        } else {
                            spanCount
                        }
                    }
                }
            }
            // disable change animation to prevent "blinking"
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        val currentFilter = viewModel.state.value.filter
                        viewModel.acceptAction(UiAction.Scroll(currentFilter))
                    }
                }
            })

            val notLoading = adapter.loadStateFlow
                .map { it.mediator?.refresh is LoadState.NotLoading || it.source.refresh is LoadState.NotLoading }
                .distinctUntilChanged()

            val hasNotScrolledForCurrentFilter = viewModel.state
                .map { it.hasNotScrolledForCurrentFilter }
                .distinctUntilChanged()


            val shouldScrollToTop = combine(
                notLoading,
                hasNotScrolledForCurrentFilter,
                Boolean::and
            ).distinctUntilChanged()

            viewLifecycleOwner.lifecycleScope.launch {
                shouldScrollToTop.collect { shouldScroll ->
                    if (shouldScroll) {
                        scrollToPosition(0)
                    }
                }
            }
        }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener {
                if (offlineLibraryModificationsAmount > 0) {
                    viewModel.synchronizeOfflineLibraryUpdates()
                }
                adapter.refresh()
            }
        }

        viewModel.doRefreshListener = { adapter.refresh() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pagingDataFlow.collect {
                    adapter.submitData(it)
                }
            }
        }

        // On page update check if the user performed a search or library update.
        // If so, scroll to the top in case of a search or to the updated entry.
        adapter.addOnPagesUpdatedListener {
            val isLoading = lastLoadState?.source?.refresh is LoadState.Loading
                    || lastLoadState?.mediator?.refresh is LoadState.Loading

            val shouldScroll =
                !viewModel.scrollToUpdatedEntryId.isNullOrBlank()

            if (!shouldScroll || isLoading) return@addOnPagesUpdatedListener

            val indexOfUpdatedEntry = adapter.snapshot()
                .indexOfFirst { (it as? LibraryEntryUiModel.EntryModel)?.entry?.id == viewModel.scrollToUpdatedEntryId }

            if (indexOfUpdatedEntry != -1) {
                binding.rvLibraryEntries.scrollToPosition(indexOfUpdatedEntry)
                viewModel.hasScrolledToUpdatedEntry()
            }
        }

        viewModel.notSynchronizedLibraryEntryModifications.observe(viewLifecycleOwner) {
            if (!viewModel.hasUser())
                return@observe

            viewModel.invalidatePagingSource()
            offlineLibraryModificationsAmount = it.size
            requireActivity().invalidateOptionsMenu()

            autoSyncDebouncer.debounce(viewLifecycleOwner.lifecycleScope) {
                // synchronize library if there are offline library updates and network is not metered
                val connectivityManager =
                    requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (it.isNotEmpty() && !connectivityManager.isActiveNetworkMetered) {
                    viewModel.synchronizeOfflineLibraryUpdates()
                }
            }
        }
    }

    override fun onItemClicked(view: View, item: LibraryEntryWithModification) {
        val media = item.libraryEntry.media
        if (media != null) {
            val detailsTransitionName = getString(R.string.details_poster_transition_name)
            val extras =
                FragmentNavigatorExtras(view.findViewById<View>(R.id.iv_thumbnail) to detailsTransitionName)
            val action =
                LibraryFragmentDirections.actionLibraryFragmentToDetailsFragment(media.toMediaDto())
            findNavController().navigateSafe(R.id.library_fragment, action, extras)
        }
    }

    override fun onItemLongClicked(item: LibraryEntryWithModification) {
        val action =
            LibraryFragmentDirections.actionLibraryFragmentToLibraryEditEntryFragment(
                item.libraryEntry.id ?: return,
                RESULT_KEY_EDIT_ENTRY_UPDATED
            )
        findNavController().navigateSafe(R.id.library_fragment, action)
    }

    override fun onEpisodeWatchedClicked(item: LibraryEntryWithModification) {
        viewModel.markEpisodeWatched(item)
    }

    override fun onEpisodeUnwatchedClicked(item: LibraryEntryWithModification) {
        viewModel.markEpisodeUnwatched(item)
    }

    override fun onRatingClicked(item: LibraryEntryWithModification) {
        viewModel.lastRatedLibraryEntry = item.libraryEntry

        val action = LibraryFragmentDirections.actionLibraryFragmentToRatingBottomSheet(
            title = item.media?.title ?: "",
            ratingTwenty = item.ratingTwenty ?: -1,
            ratingResultKey = RESULT_KEY_RATING,
            removeResultKey = RESULT_KEY_REMOVE_RATING,
            ratingSystem = RatingSystemUtil.getRatingSystem()
        )
        findNavController().navigateSafe(R.id.library_fragment, action)
    }

    private fun initSearchView(menuItem: MenuItem) {
        val searchView = menuItem.actionView as SearchView

        searchView.queryHint = getString(R.string.hint_search)

        val searchQueryText = viewModel.state.value.filter.searchQuery
        if (searchQueryText.isNotBlank()) {
            // restore previous search view state
            menuItem.expandActionView()
            searchView.post {
                searchView.setQuery(searchQueryText, false)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchDebouncer.debounce(lifecycleScope) {
                    viewModel.searchLibrary(query ?: "")
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchView.post {
                    // empty search queries triggered on collapse will be ignored
                    if (!searchView.isIconified) {
                        searchDebouncer.debounce(lifecycleScope) {
                            viewModel.searchLibrary(newText ?: "")
                        }
                    }
                }
                return false
            }
        })

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewModel.searchLibrary("")
                binding.rvLibraryEntries.apply {
                    post {
                        scrollToPosition(0)
                    }
                }
                return true
            }
        })
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        if (!viewModel.hasUser())
            return

        inflater.inflate(R.menu.library_menu, menu)
        menu.findItem(R.id.menu_synchronize).isVisible = offlineLibraryModificationsAmount > 0

        initSearchView(menu.findItem(R.id.menu_search))
    }

    @ExperimentalBadgeUtils
    override fun onPrepareMenu(menu: Menu) {
        if (menu.isEmpty())
            return

        BadgeUtils.detachBadgeDrawable(
            offlineLibraryUpdateBadge,
            binding.toolbar,
            R.id.menu_synchronize
        )

        binding.toolbar.menu.findItem(R.id.menu_synchronize).isVisible =
            offlineLibraryModificationsAmount > 0

        offlineLibraryUpdateBadge.apply {
            isVisible = offlineLibraryModificationsAmount > 0
            number = offlineLibraryModificationsAmount
        }

        BadgeUtils.attachBadgeDrawable(
            offlineLibraryUpdateBadge,
            binding.toolbar,
            R.id.menu_synchronize
        )
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_synchronize) {
            viewModel.synchronizeOfflineLibraryUpdates()
            true
        } else {
            false
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.rvLibraryEntries.smoothScrollToPosition(0)
        binding.appBarLayout.setExpanded(true)
    }

    private fun setStatusBarTransparent(setTransparent: Boolean) {
        requireActivity().setStatusBarColorRes(
            if (setTransparent)
                android.R.color.transparent
            else
                R.color.translucent_status_bar
        )
        isStatusBarTransparent = setTransparent
    }

    override fun onDestroyView() {
        viewModel.doRefreshListener = null
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
        super.onDestroyView()
    }

}