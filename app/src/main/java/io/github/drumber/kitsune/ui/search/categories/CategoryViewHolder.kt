package io.github.drumber.kitsune.ui.search.categories

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.unnamed.b.atv.model.TreeNode
import io.github.drumber.kitsune.data.model.category.CategoryNode
import io.github.drumber.kitsune.databinding.ItemCategoryNodeBinding

class CategoryViewHolder(
    context: Context,
    private val callback: (CategoryNode) -> Unit
) : TreeNode.BaseNodeViewHolder<CategoryNode>(context) {

    private lateinit var binding: ItemCategoryNodeBinding

    override fun createNodeView(node: TreeNode, value: CategoryNode): View {
        binding = ItemCategoryNodeBinding.inflate(LayoutInflater.from(context), null, false)
        binding.apply {
            tvName.text = value.parentCategory.title
            ivExpand.visibility = if(value.hasChildren()) View.VISIBLE else View.INVISIBLE
            divider.isVisible = !node.isFirstChild

            root.setOnClickListener {
                if(value.hasChildren()) {
                    ivExpand.rotation = if(node.isExpanded) 180f else 0f
                    callback(value)
                    tView.toggleNode(node)
                } else {
                    checkbox.isChecked = !checkbox.isChecked
                }
            }

            checkbox.setOnCheckedChangeListener { button, isChecked ->
                node.isSelected = isChecked
                value.isChecked = isChecked
            }
        }
        return binding.root
    }

    override fun toggle(active: Boolean) {
        binding.ivExpand.rotation = if(active) 180f else 0f
    }

    override fun toggleSelectionMode(editModeEnabled: Boolean) {
        binding.apply {
            checkbox.isChecked = mNode.isSelected
        }
    }

}