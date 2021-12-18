package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.preference.KitsunePref

class MediaItemCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs) {

    var useSizeFromPreference = true

    /** Used size if [useSizeFromPreference] is true */
    private var customItemSize = MediaItemSize.SMALL

    fun setCustomItemSize(customItemSize: MediaItemSize) {
        this.customItemSize = customItemSize
        useSizeFromPreference = false
    }

    override fun getLayoutParams(): ViewGroup.LayoutParams {
        val itemSize = if (useSizeFromPreference) {
            KitsunePref.mediaItemSize
        } else {
            customItemSize
        }
        return super.getLayoutParams().apply {
            width = resources.getDimensionPixelSize(itemSize.widthRes)
            height = resources.getDimensionPixelSize(itemSize.heightRes)
        }
    }

}