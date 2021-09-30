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
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.category.CategoryNode
import io.github.drumber.kitsune.databinding.FragmentCategoriesBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesDialogFragment : DialogFragment(R.layout.fragment_categories) {

    private val binding: FragmentCategoriesBinding by viewBinding()

    private val viewModel: CategoriesViewModel by viewModel()

    private lateinit var treeView: AndroidTreeView

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
        treeView = AndroidTreeView(requireContext(), TreeNode.root())
        treeView.setDefaultAnimation(true)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle)
        treeView.isSelectionModeEnabled = true
        var isTreeViewDataSet = false

        viewModel.categoryNodes.observe(viewLifecycleOwner) { categories ->
            if(isTreeViewDataSet) {
                // restore state after fetching a new category
                viewModel.treeViewSavedState = treeView.saveState
            }
            val root = TreeNode.root()

            categories.forEach { category ->
                addCategoryTreeNode(root, category)
            }

            val prevScrollY = binding.nestedScrollView.scrollY

            treeView.setDefaultAnimation(false)
            treeView.setRoot(root)
            binding.treeViewContainer.apply {
                removeAllViews()
                addView(treeView.view)
            }
            viewModel.treeViewSavedState?.let { treeView.restoreState(it) }
            viewModel.selectedCategories.forEach { category ->
                selectTreeNodeForCategory(root, category)
            }
            isTreeViewDataSet = true

            // restore scroll position
            binding.nestedScrollView.scrollTo(0, prevScrollY)
            treeView.setDefaultAnimation(true)
        }
    }

    private fun addCategoryTreeNode(parent: TreeNode, categoryNode: CategoryNode) {
        val node = TreeNode(categoryNode)
        val viewHolder = CategoryViewHolder(requireContext()) {
            if(it.childCategories.isEmpty()) {
                viewModel.fetchChildCategories(it)
            }
        }
        viewHolder.onSelectionChangeListener = { onNodeSelectionChange(it) }
        node.viewHolder = viewHolder
        node.isSelectable = true

        if(categoryNode.childCategories.isNotEmpty()) {
            categoryNode.childCategories.forEach { childCategory ->
                addCategoryTreeNode(node, childCategory)
            }
        }
        parent.addChild(node)
    }

    private fun selectTreeNodeForCategory(parentNode: TreeNode, category: Category): TreeNode? {
        val node = parentNode.children.find { childNode ->
            category == (childNode.value as CategoryNode).parentCategory
        }
        return if(node != null) {
            treeView.selectNode(node, true)
            node
        } else {
            parentNode.children.forEach {
                val found = selectTreeNodeForCategory(it, category)
                if(found != null) {
                    return node
                }
            }
            null
        }
    }

    private fun onNodeSelectionChange(node: TreeNode) {
        val category = (node.value as CategoryNode).parentCategory
        if(node.isSelected) {
            viewModel.addSelectedCategory(category)
        } else {
            viewModel.removeSelectedCategory(category)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setSelectedCategoriesFromTreeView()
        viewModel.storeSelectedCategories()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.treeViewSavedState = treeView.saveState
        //setSelectedCategoriesFromTreeView() // is done in onDismiss()
    }

    private fun setSelectedCategoriesFromTreeView() {
        val selectedCategories = treeView.getSelectedValues(CategoryNode::class.java).map {
            it.parentCategory
        }
        viewModel.clearSelectedCategories()
        viewModel.addAllSelectedCategories(selectedCategories)
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        if(item.itemId == R.id.unselect_all) {
            treeView.deselectAll()
            viewModel.clearSelectedCategories()
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