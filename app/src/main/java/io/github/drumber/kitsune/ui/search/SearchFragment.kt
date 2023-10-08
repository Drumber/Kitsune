package io.github.drumber.kitsune.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
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
import io.github.drumber.kitsune.databinding.FragmentSearchBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.domain.mapper.toMedia
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.MediaSearchResult
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.MediaSearchPagingAdapter
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus.*
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.lang.ref.WeakReference

class SearchFragment : BaseCollectionFragment(R.layout.fragment_search),
    FragmentDecorationPreference,
    OnItemClickListener<MediaSearchResult>,
    NavigationBarView.OnItemReselectedListener {

    override val hasTransparentStatusBar = false

    private val binding: FragmentSearchBinding by viewBinding()

    private val viewModel: SearchViewModel by activityViewModel()

    override val recyclerView: RecyclerView
        get() = binding.rvMedia

    override val resourceLoadingBinding: LayoutResourceLoadingBinding
        get() = binding.layoutLoading

    private val connectionHandler = ConnectionHandler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.apply {
            root.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            searchWrapper.initPaddingWindowInsetsListener(top = true, consume = false)
        }

        val adapter = MediaSearchPagingAdapter(Glide.with(this), this)
        setRecyclerViewAdapter(adapter)
        recyclerView.itemAnimator = null

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResultSource.collectLatest {
                adapter.submitData(it)
            }
        }

        initSearchBar()
        observeSearchBox()
        observeFilters()
        initSearchProviderStatusLayout()
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
            binding.btnSearch.icon = AppCompatResources.getDrawable(
                requireContext(),
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
                recyclerView.post {
                    // scroll to top when searching
                    recyclerView.scrollToPosition(0)
                    binding.appBarLayout.setExpanded(true)
                }
            }
        }
    }

    private fun initSearchProviderStatusLayout() {
        binding.layoutSearchProviderStatus.btnRetry.setOnClickListener {
            viewModel.initializeSearchClient()
        }

        viewModel.searchClientStatus.observe(viewLifecycleOwner) { status ->
            binding.layoutSearchProviderStatus.apply {
                root.isVisible = status != Initialized
                btnRetry.isVisible = status == Error || status == NotAvailable
                tvStatus.isVisible = btnRetry.isVisible
                progressBar.isVisible = status == NotInitialized
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun observeFilters() {
        viewModel.filtersLiveData.observe(viewLifecycleOwner) { filters ->
            val filterCount = filters?.getFilters()?.size ?: 0
            binding.btnFilter.post {
                binding.btnFilter.overlay.clear()
                val badgeDrawable = BadgeDrawable.create(requireContext()).apply {
                    isVisible = filterCount > 0
                    number = filterCount
                    verticalOffset = 90
                    horizontalOffset = 90
                }
                BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.btnFilter)
            }
        }
    }

    override fun onItemClick(view: View, item: MediaSearchResult) {
        val mediaAdapter = MediaAdapter.fromMedia(item.toMedia())
        val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(mediaAdapter)
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.search_fragment, action, extras)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
        if (recyclerView.canScrollVertically(-1)) {
            super.onNavigationItemReselected(item)
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
        super.onDestroyView()
        connectionHandler.clear()
    }

    companion object {
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
