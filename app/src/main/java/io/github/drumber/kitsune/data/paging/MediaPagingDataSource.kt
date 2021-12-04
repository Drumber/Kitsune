package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.service.Filter

abstract class MediaPagingDataSource<Value : Any>(
    filter: Filter,
    private val requestType: RequestType
): BasePagingDataSource<Value>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Value>> {
        return requestMedia(filter, requestType)
    }

    abstract suspend fun requestMedia(filter: Filter, requestType: RequestType): JSONAPIDocument<List<Value>>

}