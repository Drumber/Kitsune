package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.api.ChapterApi
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.network.toPageData
import io.github.drumber.kitsune.domain_old.service.Filter

class ChapterNetworkDataSource(
    private val chapterApi: ChapterApi
) {

    suspend fun getAllChapters(filter: Filter): PageData<NetworkChapter> {
        return chapterApi.getAllChapters(filter.options).toPageData()
    }
}