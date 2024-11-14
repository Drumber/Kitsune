package io.github.drumber.kitsune.ui.component

import androidx.core.view.isVisible
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding

fun LayoutResourceLoadingBinding.updateLoadState(
    recyclerView: RecyclerView,
    itemCount: Int,
    state: CombinedLoadStates,
    useRemoteMediator: Boolean = false,
    checkIsNotLoading: () -> Boolean = { state.refresh is LoadState.NotLoading }
) {
    val remoteState = if (useRemoteMediator) state.mediator else state.source

    val isNotLoading = checkIsNotLoading()
    recyclerView.isVisible = isNotLoading
    root.isVisible = !isNotLoading
    progressBar.isVisible = state.refresh is LoadState.Loading
    btnRetry.isVisible = remoteState?.refresh is LoadState.Error
    tvError.isVisible = remoteState?.refresh is LoadState.Error

    if (state.refresh is LoadState.NotLoading
        && state.append.endOfPaginationReached
        && itemCount < 1
    ) {
        root.isVisible = true
        tvNoData.isVisible = true
        recyclerView.isVisible = false
    } else {
        tvNoData.isVisible = false
    }
}
