package io.github.drumber.kitsune.ui.groupdetail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.databinding.FragmentGroupDetailBinding
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GroupDetailFragment : Fragment(R.layout.fragment_group_detail) {

    private val args: GroupDetailFragmentArgs by navArgs()

    private var _binding: FragmentGroupDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupDetailViewModel by viewModel {
        parametersOf(args.groupId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGroupDetailBinding.bind(view)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.nestedScrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        val glide = Glide.with(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { loading ->
                    binding.progressBar.isVisible = loading && viewModel.group.value == null
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.group.collectLatest { group ->
                    group?.let { bindGroup(it) }
                }
            }
        }
    }

    private fun bindGroup(group: Group) {
        val glide = Glide.with(this)

        glide.load(group.coverImageUrl)
            .into(binding.ivCover)

        glide.load(group.avatarUrl)
            .placeholder(R.drawable.ic_group_24)
            .into(binding.ivAvatar)

        binding.toolbar.title = group.name
        binding.tvName.text = group.name

        binding.tvTagline.apply {
            val tagline = group.tagline?.takeUnless { it.isBlank() }
            isVisible = tagline != null
            text = tagline
        }

        binding.tvMembersCount.text = resources.getQuantityString(
            R.plurals.group_members_count,
            group.membersCount,
            group.membersCount
        )

        binding.chipCategory.apply {
            val name = group.categoryName?.takeUnless { it.isBlank() }
            isVisible = name != null
            text = name
        }

        bindSection(
            header = binding.tvAboutHeader,
            content = binding.tvAbout,
            text = group.about
        )
        bindSection(
            header = binding.tvRulesHeader,
            content = binding.tvRules,
            text = group.rules
        )
    }

    private fun bindSection(header: View, content: android.widget.TextView, text: String?) {
        val value = text?.takeUnless { it.isBlank() }
        header.isVisible = value != null
        content.isVisible = value != null
        content.text = value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
