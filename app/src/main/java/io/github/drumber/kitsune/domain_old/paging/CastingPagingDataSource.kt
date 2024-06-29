package io.github.drumber.kitsune.domain_old.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.production.Casting
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.production.CastingService

class CastingPagingDataSource(
    private val service: CastingService,
    filter: Filter
) : BasePagingDataSource<Casting>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Casting>> {
        return service.allCastings(filter.options)
    }

}