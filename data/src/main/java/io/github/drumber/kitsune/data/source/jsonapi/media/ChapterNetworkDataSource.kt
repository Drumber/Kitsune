package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.api.ChapterApi
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.jsonapi.toPageData

class ChapterNetworkDataSource(
    private val chapterApi: ChapterApi
) {

    suspend fun getAllChapters(filter: Filter): PageData<NetworkChapter> {
        return chapterApi.getAllChapters(filter.options).toPageData()
    }
}