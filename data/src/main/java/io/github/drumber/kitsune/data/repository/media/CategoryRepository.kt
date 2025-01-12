package io.github.drumber.kitsune.data.repository.media

import io.github.drumber.kitsune.data.mapper.MediaMapper.toCategory
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.media.category.Category
import io.github.drumber.kitsune.data.source.jsonapi.media.CategoryNetworkDataSource

class CategoryRepository(
    private val categoryNetworkDataSource: CategoryNetworkDataSource
) {

    suspend fun getAllCategories(filter: Filter): List<Category>? {
        return categoryNetworkDataSource.getAllCategories(filter)?.map { it.toCategory() }
    }

    suspend fun getCategory(id: String, filter: Filter): Category? {
        return categoryNetworkDataSource.getCategory(id, filter)?.toCategory()
    }
}