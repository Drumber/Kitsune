package io.github.drumber.kitsune.data.model.category

data class CategoryPrefWrapper(
    val categoryId: String? = null,
    val categorySlug: String? = null,
    val parentIds: List<String>? = null
)
