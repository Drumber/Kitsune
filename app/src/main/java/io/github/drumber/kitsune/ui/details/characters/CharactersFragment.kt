package io.github.drumber.kitsune.ui.details.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.databinding.FragmentCharactersBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.adapter.paging.CharacterPagingAdapter
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharactersFragment : BaseCollectionFragment(R.layout.fragment_characters) {

    private val args: CharactersFragmentArgs by navArgs()

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharactersViewModel by viewModel()

    override val recyclerView: RecyclerView
        get() = binding.rvMedia

    override val resourceLoadingBinding: LayoutResourceLoadingBinding
        get() = binding.layoutLoading

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

        resourceLoadingBinding.btnRetry.setOnClickListener {
            viewModel.retry(args.mediaId, args.isAnime)
        }

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

        val filterAdapter = CharacterFilterAdapter(false) { language ->
            viewModel.setLanguage(language)
        }
        val pagingAdapter = CharacterPagingAdapter(Glide.with(this)) { _, character ->
            val action = CharactersFragmentDirections.actionCharactersFragmentToCharacterDetailsBottomSheet(character.toCharacterDto())
            findNavController().navigateSafe(R.id.characters_fragment, action)
        }
        val concatAdapter = ConcatAdapter(
            filterAdapter,
            pagingAdapter.applyLoadStateListenerWithLoadStateHeaderAndFooter()
        )
        setRecyclerViewAdapter(concatAdapter)

        viewModel.languages.observe(viewLifecycleOwner) { languages ->
            filterAdapter.isViewHolderVisible = languages.isNotEmpty()
            filterAdapter.languages = languages
            filterAdapter.selectedLanguage = viewModel.selectedLanguage
            filterAdapter.notifyItemChanged()
        }

        viewModel.isLoadingLanguages.observe(viewLifecycleOwner) { isLoading ->
            resourceLoadingBinding.apply {
                root.isVisible = isVisible
                progressBar.isVisible = isLoading
                tvError.isVisible = false
                btnRetry.isVisible = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataSource.collectLatest { data ->
                pagingAdapter.submitData(data)
            }
        }
    }

    override fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (recyclerView.canScrollVertically(-1)) {
            super.onNavigationItemReselected(item)
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