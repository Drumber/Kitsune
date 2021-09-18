package io.github.drumber.kitsune.ui.resourcecollection

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.model.resource.anime.Anime
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.adapter.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.widget.LoadStateSpanSizeLookup
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class ResourceCollectionFragment(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId),
    OnItemClickListener<Anime>,
    NavigationBarView.OnItemReselectedListener {

    protected val collectionViewModel: ResourceCollectionViewModel by viewModel()

    abstract val recyclerView: RecyclerView

    abstract val resourceLoadingBinding: LayoutResourceLoadingBinding?

    private lateinit var animeAdapter: AnimeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()

        recyclerView.initMarginWindowInsetsListener(left = true, right = true, consume = false)
    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)
        animeAdapter = AnimeAdapter(glide, this)
        val gridLayout = GridLayoutManager(requireContext(), 2)

        recyclerView.apply {
            layoutManager = gridLayout
            adapter = animeAdapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(animeAdapter),
                footer = ResourceLoadStateAdapter(animeAdapter)
            )
        }
        // this will make sure to display header and footer with full width
        gridLayout.spanSizeLookup = LoadStateSpanSizeLookup(animeAdapter, gridLayout.spanCount)

        lifecycleScope.launchWhenCreated {
            collectionViewModel.anime.collectLatest {
                animeAdapter.submitData(it)
            }
        }

        resourceLoadingBinding?.btnRetry?.setOnClickListener { animeAdapter.retry() }

        animeAdapter.addLoadStateListener(loadStateListener)
    }

    private val loadStateListener: (CombinedLoadStates) -> Unit = { loadState ->
        recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
        resourceLoadingBinding?.apply {
            root.isVisible = loadState.source.refresh !is LoadState.NotLoading
            progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            btnRetry.isVisible = loadState.source.refresh is LoadState.Error
            tvError.isVisible = loadState.source.refresh is LoadState.Error
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animeAdapter.removeLoadStateListener(loadStateListener)
    }

    override fun onItemClick(anime: Anime) {
        val model = ResourceAdapter.AnimeResource(anime)
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(
                with(findNavController()) { currentDestination?.id ?: graph.startDestinationId },
                inclusive = false,
                saveState = true
            )
            .setRestoreState(false)
            .build()
        onResourceClicked(model, options)
    }

    open fun onResourceClicked(model: ResourceAdapter, options: NavOptions) {}

}