package io.github.drumber.kitsune.ui.search.categories

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.view.AndroidTreeView
import io.github.drumber.kitsune.R
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
        }

        initTreeView()
    }

    private fun initTreeView() {
        treeView = AndroidTreeView(requireContext(), TreeNode.root())
        treeView.setDefaultAnimation(true)
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle)

        viewModel.categoryNodes.observe(viewLifecycleOwner) { categories ->
            viewModel.treeViewSavedState = treeView.saveState
            val root = TreeNode.root()

            categories.forEach { category ->
                addCategoryTreeNode(root, category)
            }

            treeView.setRoot(root)
            binding.treeViewContainer.apply {
                removeAllViews()
                addView(treeView.view)
            }
            viewModel.treeViewSavedState?.let { treeView.restoreState(it) }
        }
    }

    private fun addCategoryTreeNode(parent: TreeNode, categoryNode: CategoryNode) {
        val node = TreeNode(categoryNode)
        node.viewHolder = CategoryViewHolder(requireContext()) {
            if(it.childCategories.isEmpty()) {
                viewModel.fetchChildCategories(it)
            }
        }
        node.isSelectable = true

        if(categoryNode.childCategories.isNotEmpty()) {
            categoryNode.childCategories.forEach { childCategory ->
                addCategoryTreeNode(node, childCategory)
            }
        }
        parent.addChild(node)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.treeViewSavedState = treeView.saveState
    }

    companion object {
        const val TAG = "categories_dialog"

        fun showDialog(fragmentManager: FragmentManager): CategoriesDialogFragment {
            val fragment = CategoriesDialogFragment()
            fragment.show(fragmentManager, TAG)
            return fragment
        }
    }

}