package io.github.drumber.kitsune.ui.search.categories

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.category.CategoryNode
import io.github.drumber.kitsune.preference.CategoryPrefWrapper
import io.github.drumber.kitsune.ui.base.BaseDialogFragment
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.network.ResponseData
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesDialogFragment : BaseDialogFragment(0) {

    private val viewModel: CategoriesViewModel by viewModel()

    private var onDismissListener: DialogInterface.OnDismissListener? = null

    private var treeView: AndroidTreeView? = null
    private var treeRoot: TreeNode = TreeNode.root()
    private var treeContainer: FrameLayout? = null
    private var isTreeViewDataSet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        CategoriesScreen(
            isLoading = false,
            hasError = false,
            onRetry = { viewModel.fetchChildCategories(null) },
            onDismiss = { dismiss() },
            onUnselectAll = {
                treeView?.deselectAll()
                viewModel.clearSelectedCategories()
            },
            containerFactory = { context ->
                FrameLayout(context).also { frame ->
                    treeContainer = frame
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTreeView()
    }

    private fun initTreeView() {
        treeRoot = TreeNode.root()
        val tv = AndroidTreeView(requireContext(), treeRoot)
        tv.setDefaultAnimation(true)
        tv.setDefaultContainerStyle(R.style.TreeNodeStyle)
        tv.isSelectionModeEnabled = true
        treeView = tv

        viewModel.categoryNodes.observe(viewLifecycleOwner) { response ->
            if (response !is ResponseData.Success) {
                return@observe
            }
            val categories = response.data

            if (isTreeViewDataSet) {
                viewModel.treeViewSavedState = tv.saveState
            }
            treeRoot = TreeNode.root()
            categories.sortedBy { it.category.title }.forEach { addCategoryTreeNode(treeRoot, it) }

            tv.setDefaultAnimation(false)
            tv.setRoot(treeRoot)
            treeContainer?.apply {
                removeAllViews()
                addView(tv.view)
            }
            viewModel.treeViewSavedState?.let { tv.restoreState(it) }
            viewModel.selectedCategories.toSet().forEach { wrapper ->
                selectTreeNodeForCategory(treeRoot, wrapper.categoryId)
            }
            isTreeViewDataSet = true
            tv.setDefaultAnimation(true)
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
        categoryNode.childCategories
            .sortedBy { it.category.title }
            .forEach { addCategoryTreeNode(node, it) }
        parent.addChild(node)
    }

    private fun selectTreeNodeForCategory(parentNode: TreeNode, categoryId: String?): TreeNode? {
        val node = parentNode.children.find { child ->
            categoryId == (child.value as CategoryNode).category.id
        }
        if (node != null) {
            treeView?.selectNode(node, true)
            return node
        }
        parentNode.children.forEach { child ->
            val found = selectTreeNodeForCategory(child, categoryId)
            if (found != null) return found
        }
        return null
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
        val parentIds = parentCategories.map { (it.value as CategoryNode).category.id }
        val category = (childNode.value as CategoryNode).category
        return CategoryPrefWrapper(category.id, category.title, category.slug, parentIds)
    }

    private fun updateSelectionCounter(parentNode: TreeNode = treeRoot) {
        parentNode.children.forEach { child ->
            val categoryNode = child.value as CategoryNode
            if (categoryNode.hasChildren()) {
                categoryNode.category.id.let { id ->
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
            if (parent.level == targetLevel) break
            node = parent
        }
        return parentList
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.storeSelectedCategories()
        onDismissListener?.onDismiss(dialog)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.treeViewSavedState = treeView?.saveState
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
        onDismissListener = listener
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
