package io.github.drumber.kitsune.util.ui

import io.github.drumber.kitsune.R

fun getProfileSiteLogoResourceId(name: String?): Int {
    return when (name) {
        "Twitter" -> R.drawable.ic_twitter
        "Facebook" -> R.drawable.ic_facebook
        "YouTube" -> R.drawable.ic_youtube
        "Google" -> R.drawable.ic_google_plus
        "Instagram" -> R.drawable.ic_instagram
        "Twitch" -> R.drawable.ic_twitch
        "Vimeo" -> R.drawable.ic_vimeo
        "GitHub" -> R.drawable.ic_github
        "Battle.net" -> R.drawable.ic_battle_net
        "Steam" -> R.drawable.ic_steam
        "Raptr" -> R.drawable.ic_raptr
        "Discord" -> R.drawable.ic_discord
        "Tumblr" -> R.drawable.ic_tumblr
        "SoundCloud" -> R.drawable.ic_soundcloud
        "Dailymotion" -> R.drawable.ic_dailymotion
        "Kickstarter" -> R.drawable.ic_kickstarter
        "Mobcrush" -> R.drawable.ic_mobcrush
        "osu!" -> R.drawable.ic_osu
        "Patreon" -> R.drawable.ic_patreon
        "DeviantArt" -> R.drawable.ic_deviantart
        "Dribbble" -> R.drawable.ic_dribbble
        "IMDb" -> R.drawable.ic_imdb
        "Last.fm" -> R.drawable.ic_lastfm
        "Letterboxd" -> R.drawable.ic_letterboxd
        "Medium" -> R.drawable.ic_medium
        "Player.me" -> R.drawable.ic_player_me
        "Reddit" -> R.drawable.ic_reddit
        "Trakt" -> R.drawable.ic_trakt
        "Website" -> R.drawable.ic_website
        else -> R.drawable.ic_website
    }
}
