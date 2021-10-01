package io.github.drumber.kitsune.ui.search.categories

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.category.CategoryNode
import io.github.drumber.kitsune.data.model.category.CategoryPrefWrapper
import io.github.drumber.kitsune.databinding.FragmentCategoriesBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesDialogFragment : DialogFragment(R.layout.fragment_categories) {

    private val binding: FragmentCategoriesBinding by viewBinding()

    private val viewModel: CategoriesViewModel by viewModel()

    private lateinit var treeView: AndroidTreeView
    private lateinit var treeRoot: TreeNode

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.apply {
                setLayout(width, height)
                setWindowAnimations(R.style.AppTheme_Slide)
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Kitsune_FullScreenDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { dismiss() }
            toolbar.inflateMenu(R.menu.category_dialog_menu)
            toolbar.setOnMenuItemClickListener { onMenuItemClicked(it) }
        }

        initTreeView()
    }

    private fun initTreeView() {
        treeRoot = TreeNode.root()
        treeView = AndroidTreeView(requireContext(), treeRoot)
        treeView.setDefaultAnimation(true)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle)
        treeView.isSelectionModeEnabled = true
        var isTreeViewDataSet = false

        viewModel.categoryNodes.observe(viewLifecycleOwner) { categories ->
            if (isTreeViewDataSet) {
                // restore state after fetching a new category
                viewModel.treeViewSavedState = treeView.saveState
            }
            treeRoot = TreeNode.root()

            categories
                .sortedBy { it.category.title }
                .forEach { category ->
                    addCategoryTreeNode(treeRoot, category)
                }

            val prevScrollY = binding.nestedScrollView.scrollY

            treeView.setDefaultAnimation(false)
            treeView.setRoot(treeRoot)
            binding.treeViewContainer.apply {
                removeAllViews()
                addView(treeView.view)
            }
            viewModel.treeViewSavedState?.let { treeView.restoreState(it) }
            viewModel.selectedCategories.toSet().forEach { categoryWrapper ->
                selectTreeNodeForCategory(treeRoot, categoryWrapper.categoryId)
            }
            isTreeViewDataSet = true

            // restore scroll position
            binding.nestedScrollView.scrollTo(0, prevScrollY)
            treeView.setDefaultAnimation(true)

            updateSelectionCounter()
        }
    }

    private fun addCategoryTreeNode(parent: TreeNode, categoryNode: CategoryNode) {
        val node = TreeNode(categoryNode)
        val viewHolder = CategoryViewHolder(requireContext()) {
            if (it.childCategories.isEmpty()) {
                viewModel.fetchChildCategories(it)
            }
        }
        viewHolder.onSelectionChangeListener = { onNodeSelectionChange(it) }
        node.viewHolder = viewHolder
        node.isSelectable = true

        if (categoryNode.childCategories.isNotEmpty()) {
            categoryNode.childCategories
                .sortedBy { it.category.title }
                .forEach { childCategory ->
                    addCategoryTreeNode(node, childCategory)
                }
        }
        parent.addChild(node)
    }

    private fun selectTreeNodeForCategory(parentNode: TreeNode, categoryId: String?): TreeNode? {
        val node = parentNode.children.find { childNode ->
            categoryId == (childNode.value as CategoryNode).category.id
        }
        return if (node != null) {
            treeView.selectNode(node, true)
            node
        } else {
            parentNode.children.forEach {
                val found = selectTreeNodeForCategory(it, categoryId)
                if (found != null) {
                    return node
                }
            }
            null
        }
    }

    private fun onNodeSelectionChange(node: TreeNode) {
        val wrapper = getCategoryWrapper(node)
        if (node.isSelected) {
            viewModel.addSelectedCategory(wrapper)
        } else {
            viewModel.removeSelectedCategory(wrapper)
        }
        updateSelectionCounter()
    }

    private fun getCategoryWrapper(childNode: TreeNode): CategoryPrefWrapper {
        val parentCategories = findRootCategoryNodes(childNode)
        val parentIds = parentCategories.mapNotNull { (it.value as CategoryNode).category.id }
        val category = (childNode.value as CategoryNode).category
        return CategoryPrefWrapper(category.id, category.slug, parentIds)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.storeSelectedCategories()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.treeViewSavedState = treeView.saveState
    }

    private fun updateSelectionCounter(parentNode: TreeNode = treeRoot) {
        parentNode.children.forEach { child ->
            val categoryNode = child.value as CategoryNode
            if(categoryNode.hasChildren()) {
                categoryNode.category.id?.let { id ->
                    val selectedChildren = viewModel.countSelectedChildrenForParent(id)
                    val viewHolder = child.viewHolder as CategoryViewHolder
                    viewHolder.onSelectionCounterUpdate(selectedChildren)
                }
                updateSelectionCounter(child)
            }
        }
    }

    private fun findRootCategoryNodes(childNode: TreeNode, targetLevel: Int = 1): List<TreeNode> {
        val parentList = mutableListOf<TreeNode>()
        var node: TreeNode = childNode
        while (node.parent != null) {
            val parent = node.parent
            parentList.add(parent)
            if(parent.level == targetLevel) {
                break
            }
            node = parent
        }
        return parentList
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        if (item.itemId == R.id.unselect_all) {
            treeView.deselectAll()
            viewModel.clearSelectedCategories()
            updateSelectionCounter()
        } else {
            return false
        }
        return true
    }

    companion object {
        private const val TAG = "categories_dialog"

        fun showDialog(fragmentManager: FragmentManager): CategoriesDialogFragment {
            val fragment = CategoriesDialogFragment()
            fragment.show(fragmentManager, TAG)
            return fragment
        }
    }

}