package io.github.drumber.kitsune.ui.component

import androidx.core.view.isVisible
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.R
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
    val isError = remoteState?.refresh is LoadState.Error
    val isEmpty = state.refresh is LoadState.NotLoading
        && state.append.endOfPaginationReached
        && itemCount < 1

    recyclerView.isVisible = isNotLoading && !isEmpty
    root.isVisible = !isNotLoading || isEmpty
    loadingIndicator.isVisible = state.refresh is LoadState.Loading
    btnRetry.isVisible = isError
    tvError.isVisible = isError
    tvNoData.isVisible = isEmpty

    ivStateIcon.isVisible = isError || isEmpty
    if (isError) {
        ivStateIcon.setImageResource(R.drawable.ic_cloud_off_24)
    } else if (isEmpty) {
        ivStateIcon.setImageResource(R.drawable.ic_inbox_24)
    }
}
