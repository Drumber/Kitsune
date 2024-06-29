package io.github.drumber.kitsune.domain_old.model.preference

data class CategoryPrefWrapper(
    val categoryId: String? = null,
    val categoryName: String? = null,
    val categorySlug: String? = null,
    val parentIds: List<String>? = null
)
