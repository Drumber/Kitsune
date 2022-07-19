package io.github.drumber.kitsune.ui.details.characters

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentCharactersBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.adapter.paging.CharacterPagingAdapter
import io.github.drumber.kitsune.ui.base.BaseCollectionFragment
import io.github.drumber.kitsune.util.extensions.openCharacterOnMAL
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharactersFragment : BaseCollectionFragment(R.layout.fragment_characters),
    AdapterView.OnItemClickListener {

    private val args: CharactersFragmentArgs by navArgs()

    private val binding: FragmentCharactersBinding by viewBinding()

    private val viewModel: CharactersViewModel by viewModel()

    override val recyclerView: RecyclerView
        get() = binding.rvMedia

    override val resourceLoadingBinding: LayoutResourceLoadingBinding
        get() = binding.layoutLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMediaId(args.mediaId, args.isAnime)

        resourceLoadingBinding.btnRetry.setOnClickListener {
            viewModel.retry(args.mediaId, args.isAnime)
        }

        binding.toolbar.apply {
            initWindowInsetsListener(false)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        binding.languageWrapper.initPaddingWindowInsetsListener(left = true, right = true, consume = false)

        binding.autoCompleteTextView.onItemClickListener = this

        viewModel.languages.observe(viewLifecycleOwner) { languages ->
            binding.fieldLanguage.isVisible = languages.isNotEmpty()
            val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, languages)
            binding.autoCompleteTextView.apply {
                setAdapter(adapter)
                setText(viewModel.selectedLanguage, false)
            }
        }

        viewModel.isLoadingLanguages.observe(viewLifecycleOwner) { isLoading ->
            resourceLoadingBinding.apply {
                root.isVisible = isVisible
                progressBar.isVisible = isLoading
                tvError.isVisible = false
                btnRetry.isVisible = false
            }
        }

        val adapter = CharacterPagingAdapter(GlideApp.with(this)) { character ->
            character.malId?.let { malId ->
                openCharacterOnMAL(malId)
            }
        }
        setRecyclerViewAdapter(adapter)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.dataSource.collectLatest { data ->
                adapter.submitData(data)
            }
        }
    }

    override fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val language = parent.getItemAtPosition(position) as String
        viewModel.setLanguage(language)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        super.onNavigationItemReselected(item)
        binding.appBarLayout.setExpanded(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.autoCompleteTextView.apply {
            onItemClickListener = null
            setAdapter(null)
        }
    }

}