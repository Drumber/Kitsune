package io.github.drumber.kitsune.preference

import io.github.drumber.kitsune.R

enum class StartPagePref {
    Home,
    Search,
    Library,
    Profile
}

fun StartPagePref.getDestinationId() = when (this) {
    StartPagePref.Home -> R.id.main_fragment
    StartPagePref.Search -> R.id.search_fragment
    StartPagePref.Library -> R.id.library_fragment
    StartPagePref.Profile -> R.id.profile_fragment
}
