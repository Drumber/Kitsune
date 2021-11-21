package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.ItemResourceBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.ResourceViewHolder

sealed class ResourcePagingAdapter<T : Resource>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val glide: GlideRequests,
    private val listener: OnItemClickListener<T>? = null
) : PagingDataAdapter<T, ResourceViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        return ResourceViewHolder(
            ItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            glide
        ) { position ->
            getItem(position)?.let { item -> listener?.onItemClick(item) }
        }
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(ResourceAdapter.fromMedia(it)) }
    }

}