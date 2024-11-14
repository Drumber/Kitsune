package io.github.drumber.kitsune.ui.details.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.databinding.FragmentCharactersBinding
import io.github.drumber.kitsune.ui.adapter.paging.CharacterPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharactersFragment : Fragment(R.layout.fragment_characters),
    NavigationBarView.OnItemReselectedListener {

    private val args: CharactersFragmentArgs by navArgs()

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharactersViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMediaId(args.mediaId, args.isAnime)

        binding.apply {
            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(false)
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

            rvMedia.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                bottom = true,
                consume = false
            )
        }

        binding.layoutLoading.btnRetry.setOnClickListener {
            viewModel.retry(args.mediaId, args.isAnime)
        }

        val filterAdapter = CharacterFilterAdapter(false) { language ->
            viewModel.setLanguage(language)
        }
        val pagingAdapter = CharacterPagingAdapter(Glide.with(this)) { _, character ->
            val action = CharactersFragmentDirections
                .actionCharactersFragmentToCharacterDetailsBottomSheet(character.toCharacterDto())
            findNavController().navigateSafe(R.id.characters_fragment, action)
        }
        val concatAdapter = ConcatAdapter(
            filterAdapter,
            pagingAdapter.withLoadStateHeaderAndFooter(
                header = ResourceLoadStateAdapter(pagingAdapter),
                footer = ResourceLoadStateAdapter(pagingAdapter)
            )
        )
        binding.rvMedia.adapter = concatAdapter
        binding.rvMedia.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvMedia,
                        pagingAdapter.itemCount,
                        loadState
                    )
                }
            }
        }

        viewModel.languages.observe(viewLifecycleOwner) { languages ->
            filterAdapter.isViewHolderVisible = languages.isNotEmpty()
            filterAdapter.languages = languages
            filterAdapter.selectedLanguage = viewModel.selectedLanguage
            filterAdapter.notifyItemChanged()
        }

        viewModel.isLoadingLanguages.observe(viewLifecycleOwner) { isLoading ->
            binding.layoutLoading.apply {
                root.isVisible = isVisible
                progressBar.isVisible = isLoading
                tvError.isVisible = false
                btnRetry.isVisible = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataSource.collectLatest { data ->
                    pagingAdapter.submitData(data)
                }
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.rvMedia.canScrollVertically(-1)) {
            binding.rvMedia.smoothScrollToPosition(0)
            binding.appBarLayout.setExpanded(true)
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}