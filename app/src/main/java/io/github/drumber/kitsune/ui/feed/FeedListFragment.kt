package io.github.drumber.kitsune.ui.feed

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.FragmentFeedListBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.PostPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.ui.webview.WebViewFragmentDirections
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedListFragment : Fragment(R.layout.fragment_feed_list), OnItemClickListener<Post> {

    private var _binding: FragmentFeedListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedListViewModel by viewModel()

    private var isLoginRequired = false

    private val feedType: FeedType
        get() = FeedType.valueOf(
            arguments?.getString(ARG_FEED_TYPE) ?: FeedType.GLOBAL.name
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedListBinding.bind(view)

        viewModel.setFeedType(feedType)

        val adapter = PostPagingAdapter(Glide.with(this), this)
        binding.rvFeed.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvFeed.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener { adapter.refresh() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    if (isLoginRequired) return@collectLatest
                    binding.layoutLoading.updateLoadState(
                        binding.rvFeed,
                        adapter.itemCount,
                        loadState
                    )
                    binding.swipeRefreshLayout.isRefreshing =
                        loadState.refresh is LoadState.Loading && adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginRequired.collectLatest { loginRequired ->
                    isLoginRequired = loginRequired
                    binding.tvLoginRequired.isVisible = loginRequired
                    if (loginRequired) {
                        binding.rvFeed.isVisible = false
                        binding.layoutLoading.root.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataSource.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }
    }

    override fun onItemClick(view: View, item: Post) {
        val url = "${Kitsu.BASE_URL}/posts/${item.id}"
        val action = WebViewFragmentDirections.actionGlobalWebViewFragment(url)
        findNavController().navigateSafe(R.id.feed_fragment, action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_FEED_TYPE = "feed_type"

        fun newInstance(feedType: FeedType) = FeedListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEED_TYPE, feedType.name)
            }
        }
    }
}
