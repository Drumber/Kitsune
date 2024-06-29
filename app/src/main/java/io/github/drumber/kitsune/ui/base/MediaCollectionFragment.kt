package io.github.drumber.kitsune.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.domain_old.model.MediaType
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain_old.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.paging.MangaAdapter
import io.github.drumber.kitsune.ui.adapter.paging.MediaPagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class MediaCollectionFragment(
    @LayoutRes contentLayoutId: Int
) : BaseCollectionFragment(contentLayoutId), OnItemClickListener<BaseMedia> {

    abstract val collectionViewModel: MediaCollectionViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val glide = Glide.with(this)
        val adapter = when (getMediaType()) {
            MediaType.Anime -> AnimeAdapter(glide, this::onItemClick)
            MediaType.Manga -> MangaAdapter(glide, this::onItemClick)
        }
        setRecyclerViewAdapter(adapter)
        adapter.collectData()
    }

    private fun <T : BaseMedia> MediaPagingAdapter<T>.collectData(): MediaPagingAdapter<T> {
        viewLifecycleOwner.lifecycleScope.launch {
            collectionViewModel.dataSource.collectLatest { data ->
                (data as? PagingData<T>)?.let { this@collectData.submitData(it) }
            }
        }
        return this
    }

    override fun onItemClick(view: View, item: BaseMedia) {
        val model = MediaAdapter.fromMedia(item)
        onMediaClicked(view, model)
    }

    open fun onMediaClicked(view: View, model: MediaAdapter) {}

    abstract fun getMediaType(): MediaType

}