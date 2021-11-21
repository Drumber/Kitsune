package io.github.drumber.kitsune.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.paging.PagingData
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.paging.MangaAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourcePagingAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

abstract class ResourceCollectionFragment(
    @LayoutRes contentLayoutId: Int
): BaseCollectionFragment(contentLayoutId), OnItemClickListener<Resource> {

    private var dataFlowScope: Job? = null

    abstract val collectionViewModel: ResourceCollectionViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val glide = GlideApp.with(this)
        collectionViewModel.resourceSelector.observe(viewLifecycleOwner) { selector ->
            val adapter = when(selector.resourceType) {
                ResourceType.Anime -> AnimeAdapter(glide) { onItemClick(it) }.setupAdapter()
                ResourceType.Manga -> MangaAdapter(glide) { onItemClick(it) }.setupAdapter()
            }
            setRecyclerViewAdapter(adapter)
        }
    }

    private inline fun <reified T : Resource> ResourcePagingAdapter<T>.setupAdapter(): ResourcePagingAdapter<T> {
        dataFlowScope?.cancel()
        dataFlowScope = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            collectionViewModel.dataSource.collectLatest { data ->
                (data as? PagingData<T>)?.let { this@setupAdapter.submitData(it) }
            }
        }
        return this
    }

    override fun onItemClick(item: Resource) {
        val model = ResourceAdapter.fromMedia(item)
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
        onResourceClicked(model, options)
    }

    open fun onResourceClicked(model: ResourceAdapter, options: NavOptions) {}

}