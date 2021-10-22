package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.service.Filter

abstract class ResourcePagingDataSource<Value : Any>(
    filter: Filter,
    private val requestType: RequestType
): BasePagingDataSource<Value>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Value>> {
        return requestResource(filter, requestType)
    }

    abstract suspend fun requestResource(filter: Filter, requestType: RequestType): JSONAPIDocument<List<Value>>

}