package io.github.drumber.kitsune.constants

import androidx.annotation.DimenRes
import io.github.drumber.kitsune.R

enum class MediaItemSize(
    @DimenRes val widthRes: Int,
    @DimenRes val heightRes: Int
) {
    SMALL(R.dimen.media_item_width_small, R.dimen.media_item_height_small),
    MEDIUM(R.dimen.media_item_width_medium, R.dimen.media_item_height_medium),
    LARGE(R.dimen.media_item_width_large, R.dimen.media_item_height_large)
}