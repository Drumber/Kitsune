package io.github.drumber.kitsune.ui.details.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.databinding.ItemDetailsInfoRowBinding
import io.github.drumber.kitsune.databinding.SheetCharacterDetailsBinding
import io.github.drumber.kitsune.domain.model.common.media.Titles
import io.github.drumber.kitsune.domain.model.infrastructure.character.Character
import io.github.drumber.kitsune.domain.model.infrastructure.character.MediaCharacter
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.domain.model.ui.media.originalOrDown
import io.github.drumber.kitsune.domain.model.ui.media.smallOrHigher
import io.github.drumber.kitsune.ui.adapter.MediaCharacterAdapter
import io.github.drumber.kitsune.util.DataUtil.mapLanguageCodesToDisplayName
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openCharacterOnMAL
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.toPx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList

class CharacterDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterDetailsViewModel by viewModel()

    private val navArgs by navArgs<CharacterDetailsBottomSheetArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetCharacterDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMediaCharacters.adapter = MediaCharacterAdapter(
            CopyOnWriteArrayList(),
            Glide.with(this)
        ) { _, mediaCharacter ->
            val mediaAdapter = mediaCharacter.media?.let { MediaAdapter.fromMedia(it) }
                ?: return@MediaCharacterAdapter
            val action =
                CharacterDetailsBottomSheetDirections.actionCharacterDetailsBottomSheetToDetailsFragment(
                    mediaAdapter
                )
            findNavController().navigateSafe(R.id.characterDetailsBottomSheet, action)
        }

        val character = navArgs.character
        viewModel.initCharacter(character)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.characterFlow.collectLatest { character ->
                updateCharacterData(character)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                val isLoading = state.isLoadingMediaCharacters
                binding.progressBar.isVisible = isLoading
                binding.loadingWrapper.isVisible = isLoading || !state.hasMediaCharacters
                binding.tvNoData.isVisible = !isLoading && !state.hasMediaCharacters
                binding.rvMediaCharacters.isVisible = !isLoading && state.hasMediaCharacters
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteFlow.collectLatest { favorite ->
                binding.btnFavorite.setIconResource(
                    if (favorite != null) R.drawable.ic_favorite_24
                    else R.drawable.ic_favorite_border_24
                )
                TooltipCompat.setTooltipText(
                    binding.btnFavorite,
                    getString(
                        if (favorite != null) R.string.action_remove_from_favorites
                        else R.string.action_add_to_favorites
                    )
                )
            }
        }

        binding.ivCharacter.setOnClickListener {
            val fullCharacter = viewModel.characterFlow.replayCache.lastOrNull()
                ?: return@setOnClickListener

            fullCharacter.image?.originalOrDown()?.let { imageUrl ->
                openPhotoViewActivity(
                    imageUrl,
                    fullCharacter.name,
                    fullCharacter.image.smallOrHigher(),
                    binding.ivCharacter
                )
            }
        }

        binding.btnOpenOnMal.setOnClickListener {
            viewModel.characterFlow.replayCache.lastOrNull()?.malId?.let { malId ->
                openCharacterOnMAL(malId)
            }
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun updateCharacterData(character: Character) {
        binding.character = character
        updateNamesInTable(character.names)
        updateMediaCharactersRecyclerView(character.mediaCharacters)
        binding.btnOpenOnMal.isVisible = character.malId != null

        Glide.with(this)
            .load(character.image?.originalOrDown())
            .fitCenter()
            .addTransform(RoundedCorners(8.toPx()))
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(binding.ivCharacter)
    }

    private fun updateNamesInTable(names: Titles?) {
        binding.tableNames.removeViews(0, binding.tableNames.childCount - 1)

        val sortedNames = names?.filterValues { !it.isNullOrBlank() }
            ?.mapLanguageCodesToDisplayName(false)
            ?.toList()
            ?.sortedByDescending { it.first }

        sortedNames?.forEach { (language, name) ->
            val rowBinding = ItemDetailsInfoRowBinding.inflate(layoutInflater)
            rowBinding.title = language
            rowBinding.value = name
            binding.tableNames.addView(rowBinding.root, 0)
        }
    }

    private fun updateMediaCharactersRecyclerView(mediaCharacters: List<MediaCharacter>?) {
        (binding.rvMediaCharacters.adapter as MediaCharacterAdapter).apply {
            dataSet.clear()
            val sortedMediaCharacters = mediaCharacters?.sortedBy { it.role?.ordinal }
                ?: emptyList()
            dataSet.addAll(sortedMediaCharacters)
            notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}