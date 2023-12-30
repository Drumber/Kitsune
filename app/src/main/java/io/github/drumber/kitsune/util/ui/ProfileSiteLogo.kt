package io.github.drumber.kitsune.util.ui

import io.github.drumber.kitsune.R

fun getProfileSiteLogoResourceId(name: String?): Int {
    // TODO: add logos
    return when (name) {
        "GitHub" -> R.drawable.ic_github
        else -> R.drawable.ic_github // TODO: fallback logo
    }
}
