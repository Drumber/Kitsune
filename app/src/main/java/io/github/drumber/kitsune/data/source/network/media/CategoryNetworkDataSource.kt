package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.media.api.CategoryApi
import io.github.drumber.kitsune.data.source.network.media.model.category.NetworkCategory
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryNetworkDataSource(
    private val categoryApi: CategoryApi
) {

    suspend fun getAllCategories(filter: Filter): List<NetworkCategory>? {
        return withContext(Dispatchers.IO) {
            categoryApi.getAllCategories(filter.options).get()
        }
    }

    suspend fun getCategory(id: String, filter: Filter): NetworkCategory? {
        return withContext(Dispatchers.IO) {
            categoryApi.getCategory(id, filter.options).get()
        }
    }
}