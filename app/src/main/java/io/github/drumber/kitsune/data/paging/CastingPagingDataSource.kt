package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.production.Casting
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.production.CastingService

class CastingPagingDataSource(
    private val service: CastingService,
    filter: Filter
) : BasePagingDataSource<Casting>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Casting>> {
        return service.allCastings(filter.options)
    }

}