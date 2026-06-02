package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.ui.authentication.LoginViewModel
import io.github.drumber.kitsune.ui.createpost.CreatePostViewModel
import io.github.drumber.kitsune.ui.createpost.MediaPickerViewModel
import io.github.drumber.kitsune.ui.createpost.UnitPickerViewModel
import io.github.drumber.kitsune.ui.details.DetailsViewModel
import io.github.drumber.kitsune.ui.details.characters.CharacterDetailsViewModel
import io.github.drumber.kitsune.ui.details.characters.CharactersViewModel
import io.github.drumber.kitsune.ui.details.episodes.EpisodesViewModel
import io.github.drumber.kitsune.ui.details.feed.MediaFeedViewModel
import io.github.drumber.kitsune.ui.details.reactions.ReactionsViewModel
import io.github.drumber.kitsune.ui.feed.FeedListViewModel
import io.github.drumber.kitsune.ui.library.LibraryViewModel
import io.github.drumber.kitsune.ui.library.editentry.LibraryEditEntryViewModel
import io.github.drumber.kitsune.ui.main.MainActivityViewModel
import io.github.drumber.kitsune.ui.main.MainFragmentViewModel
import io.github.drumber.kitsune.ui.medialist.MediaListViewModel
import io.github.drumber.kitsune.ui.notifications.NotificationsViewModel
import io.github.drumber.kitsune.ui.onboarding.OnboardingViewModel
import io.github.drumber.kitsune.ui.postdetail.PostDetailViewModel
import io.github.drumber.kitsune.ui.profile.ProfileViewModel
import io.github.drumber.kitsune.ui.profile.UserProfileViewModel
import io.github.drumber.kitsune.ui.profile.editprofile.EditProfileViewModel
import io.github.drumber.kitsune.ui.search.SearchViewModel
import io.github.drumber.kitsune.ui.search.categories.CategoriesViewModel
import io.github.drumber.kitsune.ui.settings.AppLogsViewModel
import io.github.drumber.kitsune.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { OnboardingViewModel(get(), get(), get()) }
    viewModel { MainActivityViewModel(get(), get()) }
    viewModel { MainFragmentViewModel(get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { MediaListViewModel(get(), get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { LibraryViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LibraryEditEntryViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { (userId: String) -> UserProfileViewModel(userId, get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get(), get()) }
    viewModel { DetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { EpisodesViewModel(get(), get(), get(), get()) }
    viewModel { MediaFeedViewModel(get()) }
    viewModel { ReactionsViewModel(get(), get()) }
    viewModel { FeedListViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { NotificationsViewModel(get(), get()) }
    viewModel { PostDetailViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CreatePostViewModel(get(), get(), get()) }
    viewModel { MediaPickerViewModel(get()) }
    viewModel { UnitPickerViewModel(get()) }
    viewModel { CharactersViewModel(get(), get()) }
    viewModel { CharacterDetailsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { AppLogsViewModel() }
}
