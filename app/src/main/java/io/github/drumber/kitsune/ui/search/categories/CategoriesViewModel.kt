package io.github.drumber.kitsune.ui.search.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.presentation.model.media.category.CategoryNode
import io.github.drumber.kitsune.data.repository.CategoryRepository
import io.github.drumber.kitsune.preference.CategoryPrefWrapper
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    private val _selectedCategories =
        MutableStateFlow(KitsunePref.searchCategories.toSet())
    val selectedCategories: StateFlow<Set<CategoryPrefWrapper>> = _selectedCategories.asStateFlow()

    private val _expandedIds = MutableStateFlow(emptySet<String>())
    val expandedIds: StateFlow<Set<String>> = _expandedIds.asStateFlow()

    private val _rootNodes = MutableStateFlow<List<CategoryNode>>(emptyList())
    val rootNodes: StateFlow<List<CategoryNode>> = _rootNodes.asStateFlow()

    /** Incremented whenever the (mutable) tree structure changed, to trigger recomposition. */
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    fun storeSelectedCategories() {
        KitsunePref.searchCategories = _selectedCategories.value.toList()
    }

    fun setCategorySelected(category: CategoryPrefWrapper, isSelected: Boolean) {
        _selectedCategories.value = if (isSelected) {
            _selectedCategories.value + category
        } else {
            _selectedCategories.value.filterNot { it.categoryId == category.categoryId }.toSet()
        }
    }

    fun clearSelectedCategories() {
        _selectedCategories.value = emptySet()
    }

    fun toggleExpanded(node: CategoryNode) {
        val id = node.category.id
        val expanded = _expandedIds.value
        if (id in expanded) {
            _expandedIds.value = expanded - id
        } else {
            _expandedIds.value = expanded + id
            if (node.childCategories.isEmpty()) {
                fetchChildCategories(node)
            }
        }
    }

    fun fetchChildCategories(parent: CategoryNode?) {
        val parentId = parent?.category?.id ?: "_none"
        val filter = Filter()
            .filter("parent_id", parentId)
            .pageLimit(500)

        _isLoading.value = true
        _hasError.value = false

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val categories = categoryRepository.getAllCategories(filter)
                if (categories != null) {
                    val nodes = categories.map { CategoryNode(it) }
                        .sortedBy { it.category.title }
                    if (parent == null) {
                        _rootNodes.value = nodes
                    } else {
                        parent.childCategories.clear()
                        parent.childCategories.addAll(nodes)
                    }
                    _revision.value = _revision.value + 1
                }
            } catch (e: Exception) {
                logE("Failed to fetch categories.", e)
                _hasError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    init {
        fetchChildCategories(null)
    }
}
