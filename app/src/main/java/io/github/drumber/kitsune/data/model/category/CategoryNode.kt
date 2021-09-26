package io.github.drumber.kitsune.data.model.category

data class CategoryNode(
    val parentCategory: Category,
    val childCategories: MutableList<CategoryNode> = mutableListOf(),
    var isChecked: Boolean = false
) {

    fun hasChildren() = parentCategory.childCount?.equals(0) == false

}