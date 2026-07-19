package io.github.drumber.kitsune.ui.profile.about

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.databinding.FragmentProfileAboutBinding
import io.github.drumber.kitsune.ui.adapter.CharacterAdapter
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Encapsulates the profile favorites section (favorite anime, manga and characters lists).
 * Used by both [io.github.drumber.kitsune.ui.profile.MyProfileFragment] and [io.github.drumber.kitsune.ui.profile.UserProfileFragment], which differ only in their navigation
 * click callbacks.
 */
class ProfileFavoritesSection(
    private val binding: FragmentProfileAboutBinding,
    private val glide: RequestManager,
    private val onMediaClick: (View, Media) -> Unit,
    private val onCharacterClick: (Character) -> Unit
) {

    fun submitFavorites(favorites: List<Favorite>) {
        val favAnime = favorites.filter { it.item is Anime }.map { it.item as Anime }
        val favManga = favorites.filter { it.item is Manga }.map { it.item as Manga }
        val favCharacters = favorites.filter { it.item is Character }.map { it.item as Character }

        showFavoriteMediaInRecyclerView(binding.rvFavoriteAnime, favAnime)
        showFavoriteMediaInRecyclerView(binding.rvFavoriteManga, favManga)
        showFavoriteCharactersInRecyclerView(binding.rvFavoriteCharacters, favCharacters)

        binding.layoutFavoriteAnime.isVisible = favAnime.isNotEmpty()
        binding.layoutFavoriteManga.isVisible = favManga.isNotEmpty()
        binding.layoutFavoriteCharacters.isVisible = favCharacters.isNotEmpty()
    }

    fun clear() {
        binding.rvFavoriteAnime.adapter = null
        binding.rvFavoriteManga.adapter = null
        binding.rvFavoriteCharacters.adapter = null
    }

    private fun showFavoriteMediaInRecyclerView(recyclerView: RecyclerView, data: List<Media>) {
        if (recyclerView.adapter !is MediaRecyclerViewAdapter) {
            recyclerView.adapter = MediaRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide,
                itemSize = MediaItemSize.SMALL
            ) { view, media ->
                onMediaClick(view, media)
            }
        } else {
            val adapter = recyclerView.adapter as MediaRecyclerViewAdapter
            adapter.dataSet.clear()
            adapter.dataSet.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showFavoriteCharactersInRecyclerView(
        recyclerView: RecyclerView,
        data: List<Character>
    ) {
        if (recyclerView.adapter !is CharacterAdapter) {
            val adapter = CharacterAdapter(glide) { _, character ->
                onCharacterClick(character)
            }
            recyclerView.adapter = adapter
            adapter.submitList(data)
        } else {
            (recyclerView.adapter as CharacterAdapter).submitList(data)
        }
    }
}
