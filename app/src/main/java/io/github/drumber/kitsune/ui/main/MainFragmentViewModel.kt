package io.github.drumber.kitsune.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.github.drumber.kitsune.data.model.resource.anime.Anime
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class MainFragmentViewModel(
    private val animeService: AnimeService
) : ViewModel() {

    val trending: LiveData<List<Anime>> = liveData {
        val anime = animeService.trending(Filter()
            .pageLimit(5)
            .options).get()
        if(anime != null) {
            emit(anime)
        }
    }

}