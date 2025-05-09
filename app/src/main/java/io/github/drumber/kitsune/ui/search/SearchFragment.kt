package io.github.drumber.kitsune.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.algolia.instantsearch.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.search.model.response.ResponseSearch
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.FragmentSearchBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.MediaSearchPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.LoadStateSpanSizeLookup
import io.github.drumber.kitsune.ui.component.ResponsiveGridLayoutManager
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.Error
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.Initialized
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.NotAvailable
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.NotInitialized
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.lang.ref.WeakReference

class SearchFragment : Fragment(R.layout.fragment_search),
    FragmentDecorationPreference,
    OnItemClickListener<Media>,
    NavigationBarView.OnItemReselectedListener {

    override val hasTransparentStatusBar = false

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by activityViewModel()

    private val connectionHandler = ConnectionHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

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
        initSearchBar()
        observeSearchBox()
        observeFilters()
        initSearchProviderStatusLayout()
    }

    private fun initRecyclerView() {
        val adapter = MediaSearchPagingAdapter(Glide.with(this), this)
        val columnWidth = resources.getDimension(KitsunePref.mediaItemSize.widthRes) +
                2 * resources.getDimension(R.dimen.media_item_margin)
        val gridLayout = ResponsiveGridLayoutManager(requireContext(), columnWidth.toInt(), 2)
        gridLayout.spanSizeLookup = LoadStateSpanSizeLookup(adapter, gridLayout)

        binding.rvMedia.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ResourceLoadStateAdapter(adapter),
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvMedia.layoutManager = gridLayout
        binding.rvMedia.itemAnimator = null

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvMedia,
                        adapter.itemCount,
                        loadState
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultSource.collectLatest {
                    adapter.submitData(it)
                }
            }
        }
    }

    private fun initSearchBar() {
        binding.btnSearch.setOnClickListener {
            val isSearchFocussed = binding.searchView.getTag(TAG_SEARCH_FOCUSED) as? Boolean
            if (isSearchFocussed == true) {
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
            binding.searchView.setTag(TAG_SEARCH_FOCUSED, hasFocus)
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
            binding.rvMedia.smoothScrollToPosition(0)
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
        _binding = null
    }

    companion object {
        @SuppressLint("NonConstantResourceId")
        const val TAG_SEARCH_FOCUSED = R.drawable.ic_search_24
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
