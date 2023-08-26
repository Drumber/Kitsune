package io.github.drumber.kitsune.domain.model.ui.media

import io.github.drumber.kitsune.domain.model.infrastructure.media.category.Category

data class CategoryNode(
    val category: Category,
    val childCategories: MutableList<CategoryNode> = mutableListOf()
) {

    fun hasChildren() = category.childCount?.equals(0) == false

}