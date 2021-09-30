package io.github.drumber.kitsune.ui.search.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.category.CategoryNode
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.category.CategoryService
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesViewModel(private val categoryService: CategoryService) : ViewModel() {

    var treeViewSavedState: String? = null

    private val _selectedCategories: MutableSet<Category> = KitsunePref.searchCategories.toMutableSet()
    val selectedCategories: Set<Category>
        get() = _selectedCategories

    fun storeSelectedCategories() {
        KitsunePref.searchCategories = selectedCategories.toList()
    }

    fun addSelectedCategory(category: Category) {
        _selectedCategories.add(category)
    }

    fun addAllSelectedCategories(categories: Collection<Category>) {
        _selectedCategories.addAll(categories)
    }

    fun removeSelectedCategory(category: Category) {
        _selectedCategories.remove(category)
    }

    fun clearSelectedCategories() {
        _selectedCategories.clear()
    }

    private val _categoryNodes = MutableLiveData<List<CategoryNode>>()

    val categoryNodes: LiveData<List<CategoryNode>>
        get() = _categoryNodes

    fun fetchChildCategories(parent: CategoryNode?) {
        val parentId = if(parent == null || parent.parentCategory.id == null) {
            "_none"
        } else {
            parent.parentCategory.id
        }
        val filter = Filter()
            .filter("parent_id", parentId)
            .pageLimit(500)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = categoryService.allCategories(filter.options)
                response.get()?.let { categories ->
                    val nodes = categories.map {
                        CategoryNode(it)
                    }

                    if(parent == null) {
                        _categoryNodes.postValue(nodes)
                    } else {
                        parent.childCategories.addAll(0, nodes)
                        _categoryNodes.postValue(_categoryNodes.value)
                    }
                }
            } catch (e: Exception) {
                logE("Failed to fetch categories.", e)
            }
        }
    }

    init {
        fetchChildCategories(null)
    }

}