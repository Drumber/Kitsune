package io.github.drumber.kitsune.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaType
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.AnimeAdapter
import io.github.drumber.kitsune.ui.adapter.paging.MangaAdapter
import io.github.drumber.kitsune.ui.adapter.paging.MediaPagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class MediaCollectionFragment(
    @LayoutRes contentLayoutId: Int
) : BaseCollectionFragment(contentLayoutId), OnItemClickListener<Media> {

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

    private fun <T : Media> MediaPagingAdapter<T>.collectData(): MediaPagingAdapter<T> {
        viewLifecycleOwner.lifecycleScope.launch {
            collectionViewModel.dataSource.collectLatest { data ->
                (data as? PagingData<T>)?.let { this@collectData.submitData(it) }
            }
        }
        return this
    }

    override fun onItemClick(view: View, item: Media) {
        onMediaClicked(view, item)
    }

    open fun onMediaClicked(view: View, model: Media) {}

    abstract fun getMediaType(): MediaType

}