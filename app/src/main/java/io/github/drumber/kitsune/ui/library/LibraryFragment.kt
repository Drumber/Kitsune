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
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import io.github.drumber.kitsune.databinding.FragmentLibraryBinding
import io.github.drumber.kitsune.domain.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryKind
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryWrapper
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.paging.LibraryEntriesAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.util.RatingSystemUtil
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.showErrorSnackback
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : BaseFragment(R.layout.fragment_library, false),
    MenuProvider,
    LibraryEntriesAdapter.LibraryEntryActionListener,
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentLibraryBinding by viewBinding()

    private val viewModel: LibraryViewModel by viewModel()

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

        viewModel.userRepository.userLiveData.observe(viewLifecycleOwner) { user ->
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

        viewModel.responseListener = { response ->
            if (response is LibraryUpdateResponse.Error) {
                response.exception.showErrorSnackback(binding.rvLibraryEntries)
            }
        }

        viewModel.isUpdatingOrSyncingLibrary.observe(viewLifecycleOwner) {
            binding.progressIndicator.isVisible = it
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

    private fun initFilterChips() {
        viewModel.filter.observe(viewLifecycleOwner) { filter ->
            binding.chipMediaKind.setText(
                when (filter.kind) {
                    LibraryEntryKind.Anime -> R.string.anime
                    LibraryEntryKind.Manga -> R.string.manga
                    else -> R.string.library_kind_all
                }
            )

            filter.libraryStatus.apply {
                binding.apply {
                    chipCurrent.isChecked = contains(LibraryStatus.Current)
                    chipPlanned.isChecked = contains(LibraryStatus.Planned)
                    chipCompleted.isChecked = contains(LibraryStatus.Completed)
                    chipOnHold.isChecked = contains(LibraryStatus.OnHold)
                    chipDropped.isChecked = contains(LibraryStatus.Dropped)
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
            val statusList = KitsunePref.libraryEntryStatus.toMutableList()
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
        val prevSelected = KitsunePref.libraryEntryKind.ordinal
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_media_type)
            .setSingleChoiceItems(items, prevSelected) { dialog, which ->
                if (which != prevSelected) {
                    val kind = LibraryEntryKind.values()[which]
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
                val isNotLoading =
                    state.mediator?.refresh is LoadState.NotLoading || state.source.refresh is LoadState.NotLoading
                binding.apply {
                    rvLibraryEntries.isVisible = isNotLoading
                    layoutLoading.apply {
                        root.isVisible = !isNotLoading
                        progressBar.isVisible = state.refresh is LoadState.Loading
                        btnRetry.isVisible = state.refresh is LoadState.Error
                        tvError.isVisible = state.refresh is LoadState.Error

                        if (state.refresh is LoadState.NotLoading
                            && state.append.endOfPaginationReached
                            && adapter.itemCount < 1
                        ) {
                            root.isVisible = true
                            tvNoData.isVisible = true
                            rvLibraryEntries.isVisible = false
                        } else {
                            tvNoData.isVisible = false
                        }
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
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
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

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.dataSource.collectLatest {
                adapter.submitData(it)
            }
        }

        // On page update check if the user performed a search or library update.
        // If so, scroll to the top in case of a search or to the updated entry.
        adapter.addOnPagesUpdatedListener {
            val isLoading = lastLoadState?.source?.refresh is LoadState.Loading
                    || lastLoadState?.mediator?.refresh is LoadState.Loading

            val shouldScroll =
                !viewModel.scrollToUpdatedEntryId.isNullOrBlank() || viewModel.scrollToTopAfterSearch

            if (!shouldScroll || isLoading) return@addOnPagesUpdatedListener

            // check if we only need to scroll to the top (on search)
            if (viewModel.scrollToTopAfterSearch) {
                viewModel.scrollToTopAfterSearch = false
                binding.rvLibraryEntries.scrollToPosition(0)
                return@addOnPagesUpdatedListener
            }
            // ... else we need to scroll to a specific library entry (on update)

            val indexOfUpdatedEntry = adapter.snapshot()
                .indexOfFirst { (it as? LibraryEntryWrapper)?.libraryEntry?.id == viewModel.scrollToUpdatedEntryId }

            if (indexOfUpdatedEntry != -1) {
                binding.rvLibraryEntries.scrollToPosition(indexOfUpdatedEntry)
                viewModel.hasScrolledToUpdatedEntry()
            }
        }

        viewModel.notSynchronizedLibraryEntryModifications.observe(viewLifecycleOwner) {
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

    override fun onItemClicked(view: View, item: LibraryEntryWrapper) {
        val media = item.libraryEntry.anime ?: item.libraryEntry.manga
        if (media != null) {
            val mediaAdapter = MediaAdapter.fromMedia(media)
            val detailsTransitionName = getString(R.string.details_poster_transition_name)
            val extras =
                FragmentNavigatorExtras(view.findViewById<View>(R.id.iv_thumbnail) to detailsTransitionName)
            val action =
                LibraryFragmentDirections.actionLibraryFragmentToDetailsFragment(mediaAdapter)
            findNavController().navigateSafe(R.id.library_fragment, action, extras)
        }
    }

    override fun onItemLongClicked(item: LibraryEntryWrapper) {
        val action =
            LibraryFragmentDirections.actionLibraryFragmentToLibraryEditEntryFragment(
                item.libraryEntry.id ?: return,
                RESULT_KEY_EDIT_ENTRY_UPDATED
            )
        findNavController().navigateSafe(R.id.library_fragment, action)
    }

    override fun onEpisodeWatchedClicked(item: LibraryEntryWrapper) {
        viewModel.markEpisodeWatched(item)
    }

    override fun onEpisodeUnwatchedClicked(item: LibraryEntryWrapper) {
        viewModel.markEpisodeUnwatched(item)
    }

    override fun onRatingClicked(item: LibraryEntryWrapper) {
        viewModel.lastRatedLibraryEntry = item.libraryEntry
        val mediaAdapter =
            (item.libraryEntry.anime ?: item.libraryEntry.manga)?.let { MediaAdapter.fromMedia(it) }

        val action = LibraryFragmentDirections.actionLibraryFragmentToRatingBottomSheet(
            title = mediaAdapter?.title ?: "",
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

        val searchQueryText = viewModel.searchQuery
        if (!searchQueryText.isNullOrBlank()) {
            // restore previous search view state
            menuItem.expandActionView()
            searchView.post {
                searchView.setQuery(searchQueryText, false)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchDebouncer.debounce(lifecycleScope) {
                    viewModel.searchLibrary(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchView.post {
                    // empty search queries triggered on collapse will be ignored
                    if (!searchView.isIconified) {
                        searchDebouncer.debounce(lifecycleScope) {
                            viewModel.searchLibrary(newText)
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
                viewModel.searchLibrary(null)
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
        inflater.inflate(R.menu.library_menu, menu)
        menu.findItem(R.id.menu_synchronize).isVisible = offlineLibraryModificationsAmount > 0

        initSearchView(menu.findItem(R.id.menu_search))
    }

    @ExperimentalBadgeUtils
    override fun onPrepareMenu(menu: Menu) {
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

    override fun onDestroyView() {
        viewModel.responseListener = null
        viewModel.doRefreshListener = null
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
        super.onDestroyView()
    }

}