package io.github.drumber.kitsune.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.preference.KitsunePref
import kotlin.math.roundToInt

class MediaItemCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs) {

    var isInGridLayout = false

    /** Used size if [isInGridLayout] is false */
    private var customItemSize = MediaItemSize.SMALL

    fun setCustomItemSize(customItemSize: MediaItemSize) {
        this.customItemSize = customItemSize
    }

    override fun getLayoutParams(): ViewGroup.LayoutParams {
        return super.getLayoutParams().apply {
            if (!isInGridLayout) {
                width = resources.getDimensionPixelSize(customItemSize.widthRes)
                height = resources.getDimensionPixelSize(customItemSize.heightRes)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isInGridLayout) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val width = MeasureSpec.getSize(widthMeasureSpec)

        // original size from preference to calculate the height in the correct aspect ratio
        val origWidth = resources.getDimensionPixelSize(KitsunePref.mediaItemSize.widthRes)
        val origHeight = resources.getDimensionPixelSize(KitsunePref.mediaItemSize.heightRes)

        val newHeight = ((origHeight.toFloat() / origWidth.toFloat()) * width).roundToInt()
        val newHeightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightSpec)
    }

}