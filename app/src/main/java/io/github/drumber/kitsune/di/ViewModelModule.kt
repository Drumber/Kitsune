package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.ui.authentication.LoginViewModel
import io.github.drumber.kitsune.ui.details.DetailsViewModel
import io.github.drumber.kitsune.ui.library.LibraryViewModel
import io.github.drumber.kitsune.ui.main.MainActivityViewModel
import io.github.drumber.kitsune.ui.main.MainFragmentViewModel
import io.github.drumber.kitsune.ui.main.explore.ExploreViewModel
import io.github.drumber.kitsune.ui.profile.ProfileViewModel
import io.github.drumber.kitsune.ui.search.SearchViewModel
import io.github.drumber.kitsune.ui.search.categories.CategoriesViewModel
import io.github.drumber.kitsune.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainActivityViewModel() }
    viewModel { MainFragmentViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { ExploreViewModel(get(), get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { LibraryViewModel(get(), get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { DetailsViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}