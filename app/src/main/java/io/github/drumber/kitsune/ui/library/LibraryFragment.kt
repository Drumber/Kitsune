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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.algolia.instantsearch.core.searcher.Debouncer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.data.model.library.LibraryEntryKind
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.FragmentLibraryBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.paging.LibraryEntriesAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.showErrorSnackback
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : BaseFragment(R.layout.fragment_library, false),
    LibraryEntriesAdapter.LibraryEntryActionListener,
    NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentLibraryBinding by viewBinding()

    private val viewModel: LibraryViewModel by viewModel()

    private var offlineLibraryUpdatesAmount = 0
    private lateinit var offlineLibraryUpdateBadge: BadgeDrawable

    private val searchDebouncer by lazy { Debouncer(300L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offlineLibraryUpdateBadge = BadgeDrawable.create(requireContext())
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

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

        setFragmentResultListener(RatingBottomSheet.RATING_REQUEST_KEY) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateRating(rating)
            }
        }

        setFragmentResultListener(RatingBottomSheet.REMOVE_RATING_REQUEST_KEY) { _, _ ->
            viewModel.updateRating(null)
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
                    chipCurrent.isChecked = contains(Status.Current)
                    chipPlanned.isChecked = contains(Status.Planned)
                    chipCompleted.isChecked = contains(Status.Completed)
                    chipOnHold.isChecked = contains(Status.OnHold)
                    chipDropped.isChecked = contains(Status.Dropped)
                }
            }
        }

        binding.apply {
            chipMediaKind.setOnClickListener { showMediaSelectorDialog() }
            chipCurrent.initStatusClickListener(Status.Current)
            chipPlanned.initStatusClickListener(Status.Planned)
            chipCompleted.initStatusClickListener(Status.Completed)
            chipOnHold.initStatusClickListener(Status.OnHold)
            chipDropped.initStatusClickListener(Status.Dropped)
        }
    }

    private fun Chip.initStatusClickListener(status: Status) {
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
        val glide = GlideApp.with(this)
        val adapter = LibraryEntriesAdapter(glide, this)

        adapter.addLoadStateListener { state ->
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

                    // TODO: this only scrolls to the top after the first load finished,
                    //       we also need to scroll to top after the last load finished
                    if (isNotLoading && viewModel.scrollToTopAfterSearch) {
                        rvLibraryEntries.scrollToPosition(0)
                        viewModel.scrollToTopAfterSearch = false
                    }
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
                if (offlineLibraryUpdatesAmount > 0) {
                    viewModel.synchronizeOfflineLibraryUpdates()
                }
                adapter.refresh()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.dataSource.collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.offlineLibraryUpdateDao.getAllOfflineLibraryUpdatesLiveData()
            .observe(viewLifecycleOwner) {
                viewModel.invalidatePagingSource()
                offlineLibraryUpdatesAmount = it.size
                requireActivity().invalidateOptionsMenu()

                // synchronize library if there are offline library updates and network is not metered
                val connectivityManager =
                    requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (it.isNotEmpty() && !connectivityManager.isActiveNetworkMetered) {
                    viewModel.synchronizeOfflineLibraryUpdates()
                }
            }
    }

    override fun onItemClicked(item: LibraryEntryWrapper) {
        val media = item.libraryEntry.anime ?: item.libraryEntry.manga
        if (media != null) {
            val mediaAdapter = MediaAdapter.fromMedia(media)
            val action =
                LibraryFragmentDirections.actionLibraryFragmentToDetailsFragment(mediaAdapter)
            findNavController().navigateSafe(R.id.library_fragment, action)
        }
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
        val sheetLibraryRating = RatingBottomSheet()
        val bundle = bundleOf(
            RatingBottomSheet.BUNDLE_TITLE to mediaAdapter?.title,
            RatingBottomSheet.BUNDLE_RATING to item.ratingTwenty
        )
        sheetLibraryRating.arguments = bundle
        sheetLibraryRating.show(parentFragmentManager, RatingBottomSheet.TAG)
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
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.library_menu, menu)
        menu.findItem(R.id.menu_synchronize).isVisible = offlineLibraryUpdatesAmount > 0

        initSearchView(menu.findItem(R.id.menu_search))

        super.onCreateOptionsMenu(menu, inflater)
    }

    @ExperimentalBadgeUtils
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        BadgeUtils.detachBadgeDrawable(
            offlineLibraryUpdateBadge,
            binding.toolbar,
            R.id.menu_synchronize
        )

        binding.toolbar.menu.findItem(R.id.menu_synchronize).isVisible =
            offlineLibraryUpdatesAmount > 0

        offlineLibraryUpdateBadge.apply {
            isVisible = offlineLibraryUpdatesAmount > 0
            number = offlineLibraryUpdatesAmount
        }

        BadgeUtils.attachBadgeDrawable(
            offlineLibraryUpdateBadge,
            binding.toolbar,
            R.id.menu_synchronize
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_synchronize) {
            viewModel.synchronizeOfflineLibraryUpdates()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.rvLibraryEntries.smoothScrollToPosition(0)
        binding.appBarLayout.setExpanded(true)
    }

    override fun onDestroyView() {
        viewModel.responseListener = null
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
        super.onDestroyView()
    }

}