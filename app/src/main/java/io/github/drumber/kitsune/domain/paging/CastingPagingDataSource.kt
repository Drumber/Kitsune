package io.github.drumber.kitsune.domain.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.production.Casting
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.production.CastingService

class CastingPagingDataSource(
    private val service: CastingService,
    filter: Filter
) : BasePagingDataSource<Casting>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Casting>> {
        return service.allCastings(filter.options)
    }

}