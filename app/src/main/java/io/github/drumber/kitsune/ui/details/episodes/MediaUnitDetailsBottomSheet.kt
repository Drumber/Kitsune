package io.github.drumber.kitsune.ui.details.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.dto.MediaUnitDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaUnit
import io.github.drumber.kitsune.ui.compose.composeView

class MediaUnitDetailsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mediaUnitDto: MediaUnitDto? = arguments?.let {
            BundleCompat.getParcelable(it, BUNDLE_MEDIA_UNIT_ADAPTER, MediaUnitDto::class.java)
        }
        val mediaUnit = mediaUnitDto?.toMediaUnit()
        val thumbnailUrl = mediaUnit?.thumbnail?.smallOrHigher() ?: arguments?.getString(BUNDLE_THUMBNAIL)
        return composeView {
            MediaUnitDetailsScreen(mediaUnit = mediaUnit, thumbnailUrl = thumbnailUrl)
        }
    }

    companion object {
        const val TAG = "media_unit_details_bottom_sheet"
        const val BUNDLE_MEDIA_UNIT_ADAPTER = "media_unit_adapter_bundle_key"
        const val BUNDLE_THUMBNAIL = "thumbnail_bundle_key"
    }
}
