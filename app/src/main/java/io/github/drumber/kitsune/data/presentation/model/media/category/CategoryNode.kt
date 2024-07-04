package io.github.drumber.kitsune.data.presentation.model.media.category

data class CategoryNode(
    val category: Category,
    val childCategories: MutableList<CategoryNode> = mutableListOf()
) {

    fun hasChildren() = category.childCount?.equals(0) == false
}