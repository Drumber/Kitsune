package io.github.drumber.kitsune.ui.details.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharactersFragment : Fragment(R.layout.fragment_characters),
    NavigationBarView.OnItemReselectedListener {

    private val args: CharactersFragmentArgs by navArgs()

    private val viewModel: CharactersViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val items = viewModel.dataSource.collectAsLazyPagingItems()
        val languages by viewModel.languages.collectAsStateWithLifecycle(emptyList<String>())
        CharactersScreen(
            title = getString(R.string.title_characters),
            items = items,
            languages = languages.orEmpty(),
            selectedLanguage = viewModel.selectedLanguage,
            onNavigateUp = { findNavController().navigateUp() },
            onLanguageSelected = { viewModel.setLanguage(it) },
            onCharacterClick = { character ->
                val action = CharactersFragmentDirections
                    .actionCharactersFragmentToCharacterDetailsBottomSheet(character.toCharacterDto())
                findNavController().navigateSafe(R.id.characters_fragment, action)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMediaId(args.mediaId, args.isAnime)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        findNavController().navigateUp()
    }
}
