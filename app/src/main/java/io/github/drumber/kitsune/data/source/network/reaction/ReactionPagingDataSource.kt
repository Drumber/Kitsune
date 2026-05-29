package io.github.drumber.kitsune.data.source.network.reaction

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction

class ReactionPagingDataSource(
    private val dataSource: ReactionNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkMediaReaction>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkMediaReaction> {
        return dataSource.getMediaReactions(filter.pageOffset(pageOffset))
    }
}
