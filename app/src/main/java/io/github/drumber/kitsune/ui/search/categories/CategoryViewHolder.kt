package io.github.drumber.kitsune.ui.search.categories

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.unnamed.b.atv.model.TreeNode
import io.github.drumber.kitsune.domain.model.ui.media.CategoryNode
import io.github.drumber.kitsune.databinding.ItemCategoryNodeBinding

class CategoryViewHolder(
    context: Context,
    private val callback: (CategoryNode) -> Unit
) : TreeNode.BaseNodeViewHolder<CategoryNode>(context) {

    private lateinit var binding: ItemCategoryNodeBinding

    var onSelectionChangeListener: ((TreeNode) -> Unit)? = null

    override fun createNodeView(node: TreeNode, value: CategoryNode): View {
        binding = ItemCategoryNodeBinding.inflate(LayoutInflater.from(context), null, false)
        binding.apply {
            tvName.text = value.category.title
            ivExpand.visibility = if(value.hasChildren()) View.VISIBLE else View.INVISIBLE
            divider.isVisible = !node.isFirstChild
            checkbox.isVisible = node.level > 1
            checkbox.isChecked = node.isSelected

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
                onSelectionChangeListener?.invoke(node)
            }
        }
        return binding.root
    }

    fun onSelectionCounterUpdate(selectedChildren: Int) {
        if(!this::binding.isInitialized) return
        binding.tvCounter.apply {
            text = if (selectedChildren < 100) {
                selectedChildren.toString()
            } else {
                "99+"
            }
            isVisible = selectedChildren > 0
        }
    }

    override fun toggle(active: Boolean) {
        binding.ivExpand.rotation = if(active) 180f else 0f
    }

    override fun toggleSelectionMode(editModeEnabled: Boolean) {
        binding.apply {
            checkbox.isChecked = mNode.isSelected
            checkbox.jumpDrawablesToCurrentState() // prevent checkbox animation
        }
    }

}