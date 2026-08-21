package io.github.drumber.kitsune.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import com.algolia.instantsearch.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.search.model.response.ResponseSearch
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.algolia.SearchType
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.UserSearchResult
import io.github.drumber.kitsune.databinding.FragmentSearchBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.MediaSearchPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.adapter.paging.UserSearchPagingAdapter
import io.github.drumber.kitsune.ui.component.LoadStateSpanSizeLookup
import io.github.drumber.kitsune.ui.component.ResponsiveGridLayoutManager
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.Error
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.Initialized
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.NotAvailable
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.NotInitialized
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.qualifier.named
import java.lang.ref.WeakReference
import kotlin.math.max

class SearchFragment : Fragment(R.layout.fragment_search),
    FragmentDecorationPreference,
    OnItemClickListener<Media>,
    NavigationBarView.OnItemReselectedListener {

    override val hasTransparentStatusBar = false

    private val binding by viewBinding(FragmentSearchBinding::bind)

    private val viewModel: SearchViewModel by activityViewModel()

    private val args: SearchFragmentArgs by navArgs()

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())

    private val connectionHandler = ConnectionHandler()

    private var pendingSearchFocus = false

    private var isSearchViewFocused = false

    private lateinit var mediaAdapter: MediaSearchPagingAdapter
    private lateinit var userAdapter: UserSearchPagingAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var listLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        if (findNavController().currentBackStackEntry?.arguments == null) {
            view.doOnPreDraw { startPostponedEnterTransition() }
        } else {
            // safeguard: ensure startPostponedEnterTransition() got called within 200ms
            view.postDelayed({
                startPostponedEnterTransition()
            }, 200)
        }

        binding.apply {
            root.initPaddingWindowInsetsListener(
                left = true,
                top = true,
                right = true,
                consume = false
            )
            rvMedia.initPaddingWindowInsetsListener(bottom = true, consume = false)
        }

        initRecyclerView()
        initSearchTypeToggle()
        initSearchBar()
        observeSearchBox()
        observeFilters()
        initSearchProviderStatusLayout()

        if (savedInstanceState == null && args.focusSearch) {
            pendingSearchFocus = true
            binding.searchView.post {
                if (isAdded) focusSearchView()
            }
        }
    }

    private fun initRecyclerView() {
        val adapter = MediaSearchPagingAdapter(listener = this)
        mediaAdapter = adapter
        userAdapter = UserSearchPagingAdapter(imageLoader) { _, item ->
            onUserClick(item)
        }

        initLayoutManagers()
        gridLayoutManager.spanSizeLookup = LoadStateSpanSizeLookup(adapter, gridLayoutManager)

        binding.rvMedia.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ResourceLoadStateAdapter(adapter),
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvMedia.layoutManager = gridLayoutManager
        binding.rvMedia.itemAnimator = null

        binding.layoutLoading.btnRetry.setOnClickListener {
            if (viewModel.currentSearchType.value == SearchType.Users) {
                userAdapter.retry()
            } else {
                mediaAdapter.retry()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    if (viewModel.currentSearchType.value == SearchType.Users) return@collectLatest
                    binding.layoutLoading.updateLoadState(
                        binding.rvMedia,
                        adapter.itemCount,
                        loadState
                    )

                    if (loadState.refresh is LoadState.NotLoading) {
                        binding.rvMedia.doOnPreDraw { startPostponedEnterTransition() }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userAdapter.loadStateFlow.collectLatest { loadState ->
                    if (viewModel.currentSearchType.value != SearchType.Users) return@collectLatest
                    binding.layoutLoading.updateLoadState(
                        binding.rvMedia,
                        userAdapter.itemCount,
                        loadState
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultSource.collectLatest { data ->
                    if (viewModel.currentSearchType.value == SearchType.Users) {
                        userAdapter.submitData(data.map { it as UserSearchResult })
                    } else {
                        mediaAdapter.submitData(data.map { it as Media })
                    }
                }
            }
        }
    }

    private fun initLayoutManagers() {
        val columnWidth = resources.getDimension(KitsunePref.mediaItemSize.widthRes) +
                2 * resources.getDimension(R.dimen.media_item_margin)
        val mediaItemHeight = resources.getDimension(KitsunePref.mediaItemSize.heightRes).toInt()

        // Add extra space at the top to draw items behind the transparent topbar
        gridLayoutManager = object : ResponsiveGridLayoutManager(
            context = requireContext(),
            columnWidth = columnWidth.toInt(),
            minColumns = 2,
        ) {
            override fun calculateExtraLayoutSpace(
                state: RecyclerView.State,
                extraLayoutSpace: IntArray
            ) {
                super.calculateExtraLayoutSpace(state, extraLayoutSpace)
                extraLayoutSpace[0] = max(extraLayoutSpace[0], mediaItemHeight)
            }
        }

        listLayoutManager = object : LinearLayoutManager(requireContext()) {
            override fun calculateExtraLayoutSpace(
                state: RecyclerView.State,
                extraLayoutSpace: IntArray
            ) {
                super.calculateExtraLayoutSpace(state, extraLayoutSpace)
                extraLayoutSpace[0] = max(extraLayoutSpace[0], mediaItemHeight)
            }
        }
    }

    private fun initSearchTypeToggle() {
        val checkedId = if (viewModel.currentSearchType.value == SearchType.Users) {
            R.id.btn_search_users
        } else {
            R.id.btn_search_media
        }
        binding.toggleSearchType.check(checkedId)
        applySearchType(viewModel.currentSearchType.value ?: SearchType.Media)

        binding.toggleSearchType.addOnButtonCheckedListener { _, id, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val searchType = if (id == R.id.btn_search_users) {
                SearchType.Users
            } else {
                SearchType.Media
            }
            if (viewModel.currentSearchType.value == searchType) return@addOnButtonCheckedListener
            applySearchType(searchType)
            viewModel.switchSearchType(searchType)
        }
    }

    private fun applySearchType(searchType: SearchType) {
        binding.btnFilter.isVisible = searchType != SearchType.Users
        if (searchType == SearchType.Users) {
            binding.rvMedia.layoutManager = listLayoutManager
            binding.rvMedia.adapter = userAdapter.withLoadStateFooter(
                footer = ResourceLoadStateAdapter(userAdapter)
            )
        } else {
            binding.rvMedia.layoutManager = gridLayoutManager
            binding.rvMedia.adapter = mediaAdapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(mediaAdapter),
                footer = ResourceLoadStateAdapter(mediaAdapter)
            )
        }
    }

    private fun onUserClick(user: UserSearchResult) {
        val action = io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(user.id, user.name)
        findNavController().navigateSafe(R.id.search_fragment, action)
    }

    private fun initSearchBar() {
        binding.btnSearch.setOnClickListener {
            if (isSearchViewFocused) {
                val focusedView = binding.searchView.findFocus()
                focusedView.clearFocus()
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            } else {
                focusSearchView()
            }
        }

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            binding.btnSearch.setImageResource(
                if (hasFocus) R.drawable.ic_arrow_back_24 else R.drawable.ic_search_24
            )
            isSearchViewFocused = hasFocus
        }

        binding.btnFilter.apply {
            setOnClickListener {
                val action = SearchFragmentDirections.actionSearchFragmentToFacetFragment()
                findNavController().navigateSafe(R.id.search_fragment, action)
            }
            setOnLongClickListener {
                if (!viewModel.filtersLiveData.value?.getFilters().isNullOrEmpty()) {
                    viewModel.clearSearchFilter()
                    return@setOnLongClickListener true
                }
                false
            }
        }
    }

    private fun observeSearchBox() {
        viewModel.searchBox.observe(viewLifecycleOwner) { searchBox ->
            // clear previous search box connection (e.g. when switching search type)
            connectionHandler.clear()
            val searchBoxView = SearchBoxViewAppCompat(binding.searchView)
            connectionHandler += searchBox.connectView(searchBoxView)
            connectionHandler += SearchResponseListener(searchBox) {
                binding.rvMedia.post {
                    if (!isAdded) return@post
                    // scroll to top when searching
                    binding.rvMedia.scrollToPosition(0)
                    binding.appBarLayout.setExpanded(true)
                }
            }

            // connecting the search box re-applies the query text and drops the
            // initial keyboard focus -> re-assert it once when opened from Home
            if (pendingSearchFocus) {
                pendingSearchFocus = false
                binding.searchView.post {
                    if (isAdded) focusSearchView()
                }
            }
        }
    }

    private fun initSearchProviderStatusLayout() {
        binding.layoutSearchProviderStatus.btnRetrySearchProvider.setOnClickListener {
            viewModel.initializeSearchClient()
        }

        viewModel.searchClientStatus.observe(viewLifecycleOwner) { status ->
            binding.layoutSearchProviderStatus.apply {
                root.isVisible = status != Initialized
                btnRetrySearchProvider.isVisible = status == Error || status == NotAvailable
                tvStatus.isVisible = btnRetrySearchProvider.isVisible
                progressBarSearchProvider.isVisible = status == NotInitialized
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun observeFilters() {
        viewModel.filtersLiveData.observe(viewLifecycleOwner) { filters ->
            val filterCount = filters?.getFilters()?.size ?: 0
            binding.btnFilter.post {
                if (!isAdded) return@post
                binding.btnFilter.overlay.clear()
                val badgeDrawable = BadgeDrawable.create(binding.btnFilter.context).apply {
                    isVisible = filterCount > 0
                    number = filterCount
                }
                BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.btnFilter)
            }
        }
    }

    override fun onItemClick(view: View, item: Media) {
        val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(item.toMediaDto())
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.search_fragment, action, extras)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
        if (binding.rvMedia.canScrollVertically(-1)) {
            binding.rvMedia.smoothScrollOrJumpToTop()
        } else {
            focusSearchView()
        }
    }

    private fun focusSearchView() {
        binding.searchView.requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        connectionHandler.clear()
        super.onDestroyView()
    }

    /**
     * Triggers the onSearchReceived callback after the
     * search query was changed AND the response is received.
     */
    private class SearchResponseListener(
        searchBox: SearchBoxConnector<ResponseSearch>,
        private val onSearchReceived: () -> Unit
    ) : AbstractConnection() {

        private val _searchBox = WeakReference(searchBox)
        private var pendingSearch = false

        private val onQueryChanged = { _: Any? ->
            pendingSearch = true
        }
        private val onSearchResponse = { r: ResponseSearch? ->
            // new data was received while there is a pending search, so notify the callback
            if (pendingSearch) {
                onSearchReceived()
            }
            // reset pendingSearch flag when the first page was received
            if (pendingSearch && r?.pageOrNull == 0) {
                pendingSearch = false
            }
        }

        override fun connect() {
            super.connect()
            _searchBox.get()?.let {
                it.viewModel.query.subscribe(onQueryChanged)
                it.searcher.response.subscribe(onSearchResponse)
            }
        }

        override fun disconnect() {
            super.disconnect()
            _searchBox.get()?.let {
                it.viewModel.query.unsubscribe(onQueryChanged)
                it.searcher.response.unsubscribe(onSearchResponse)
            }
        }

    }

}
