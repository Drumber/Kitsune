package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.github.drumber.kitsune.databinding.ItemNetworkStateBinding

class ResourceLoadStateAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private val adapter: PagingDataAdapter<T, VH>,
) : LoadStateAdapter<ResourceLoadStateAdapter<T, VH>.NetworkStateItemViewHolder>() {

    override fun onBindViewHolder(
        holder: NetworkStateItemViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
        // support full width for StaggeredGridLayout
        (holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateItemViewHolder {
        return NetworkStateItemViewHolder(
            ItemNetworkStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ) { adapter.retry() }
    }


    inner class NetworkStateItemViewHolder(
        private val binding: ItemNetworkStateBinding,
        private val retryCallback: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { retryCallback() }
        }

        fun bind(loadState: LoadState) {
            with(binding) {
                progressBar.isVisible = loadState is LoadState.Loading
                btnRetry.isVisible = loadState is LoadState.Error
                tvError.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
                tvError.text = (loadState as? LoadState.Error)?.error?.message
            }
        }

    }

}