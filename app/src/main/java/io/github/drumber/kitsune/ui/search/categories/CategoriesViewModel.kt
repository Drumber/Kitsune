package io.github.drumber.kitsune.ui.search.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.model.category.CategoryNode
import io.github.drumber.kitsune.data.model.category.CategoryPrefWrapper
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.category.CategoryService
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.network.ResponseData
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriesViewModel(private val categoryService: CategoryService) : ViewModel() {

    var treeViewSavedState: String? = null

    private val _selectedCategories: MutableSet<CategoryPrefWrapper> = KitsunePref.searchCategories.toMutableSet()
    val selectedCategories: Set<CategoryPrefWrapper>
        get() = _selectedCategories

    fun storeSelectedCategories() {
        KitsunePref.searchCategories = selectedCategories.toList()
    }

    fun addSelectedCategory(category: CategoryPrefWrapper) {
        _selectedCategories.add(category)
    }

    fun removeSelectedCategory(category: CategoryPrefWrapper) {
        _selectedCategories.remove(category)
    }

    fun clearSelectedCategories() {
        _selectedCategories.clear()
    }

    fun countSelectedChildrenForParent(parentId: String): Int {
        return selectedCategories.filter { it.parentIds?.contains(parentId) == true }.size
    }

    private val _categoryNodes = MutableLiveData<ResponseData<List<CategoryNode>>>()

    val categoryNodes: LiveData<ResponseData<List<CategoryNode>>>
        get() = _categoryNodes

    fun fetchChildCategories(parent: CategoryNode?) {
        val parentId = if (parent == null || parent.category.id == null) {
            "_none"
        } else {
            parent.category.id
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

                    if (parent == null) {
                        _categoryNodes.postValue(ResponseData.Success(nodes))
                    } else {
                        parent.childCategories.addAll(0, nodes)
                        if (_categoryNodes.value is ResponseData.Success || _categoryNodes.value?.data == null) {
                            _categoryNodes.postValue(_categoryNodes.value)
                        } else {
                            val responseData = ResponseData.Success(categoryNodes.value?.data!!)
                            _categoryNodes.postValue(responseData)
                        }
                    }
                }
            } catch (e: Exception) {
                logE("Failed to fetch categories.", e)
                _categoryNodes.postValue(ResponseData.Error(e, categoryNodes.value?.data))
            }
        }
    }

    init {
        fetchChildCategories(null)
    }

}