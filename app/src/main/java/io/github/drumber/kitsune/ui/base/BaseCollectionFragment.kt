package io.github.drumber.kitsune.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.ui.adapter.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.MangaAdapter
import io.github.drumber.kitsune.ui.adapter.ResourcePagingAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

abstract class BaseCollectionFragment(
    @LayoutRes contentLayoutId: Int
): ResourceCollectionFragment(contentLayoutId) {

    private var dataFlowScope: Job? = null

    abstract val collectionViewModel: BaseCollectionViewModel

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

}