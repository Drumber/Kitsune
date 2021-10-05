package io.github.drumber.kitsune.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.widget.LoadStateSpanSizeLookup
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import kotlin.math.floor
import kotlin.math.max

abstract class ResourceCollectionFragment(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId),
    OnItemClickListener<Resource>,
    View.OnClickListener,
    NavigationBarView.OnItemReselectedListener {

    abstract val recyclerView: RecyclerView

    abstract val resourceLoadingBinding: LayoutResourceLoadingBinding?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        recyclerView.initMarginWindowInsetsListener(left = true, right = true, consume = false)
    }

    fun setRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
        val oldAdapter = recyclerView.adapter
        if(oldAdapter is PagingDataAdapter<*, *>) {
            oldAdapter.removeLoadStateListener(loadStateListener)
        }

        recyclerView.adapter = if(adapter is PagingDataAdapter<*, *>) {
            adapter.addLoadStateListener(loadStateListener)

            val gridLayout = recyclerView.layoutManager as GridLayoutManager
            // this will make sure to display header and footer with full width
            gridLayout.spanSizeLookup = LoadStateSpanSizeLookup(adapter, gridLayout.spanCount)

            adapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(adapter),
                footer = ResourceLoadStateAdapter(adapter)
            )
        } else {
            adapter
        }
    }

    private fun initView() {
        val gridLayout = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayout
        recyclerView.post {
            if (isAdded) {
                // calculate span count in relation to the recycler view width
                val width = recyclerView.width
                val cellWidth = resources.getDimension(R.dimen.resource_item_width) +
                        2 * resources.getDimension(R.dimen.resource_item_margin)
                val spanCount = floor(width / cellWidth).toInt()
                gridLayout.spanCount = max(2, spanCount) // set new span count with minimum 2 columns
            }
        }

        resourceLoadingBinding?.btnRetry?.setOnClickListener(this)
    }

    /** Triggered when clicking on retry button. */
    override fun onClick(retryButton: View?) {
        (recyclerView.adapter as? PagingDataAdapter<*, *>)?.retry()
    }

    private val loadStateListener: (CombinedLoadStates) -> Unit = { loadState ->
        if(view?.parent != null) {
            recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
            resourceLoadingBinding?.apply {
                root.isVisible = loadState.source.refresh !is LoadState.NotLoading
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
                tvError.isVisible = loadState.source.refresh is LoadState.Error
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (recyclerView.adapter as? PagingDataAdapter<*, *>)?.removeLoadStateListener(loadStateListener)
    }

    override fun onItemClick(resource: Resource) {
        val model = ResourceAdapter.fromResource(resource)
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