package io.github.drumber.kitsune.ui.details.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import coil3.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.dto.MediaUnitDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaUnit
import io.github.drumber.kitsune.databinding.SheetMediaUnitDetailsBinding
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity

class MediaUnitDetailsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SheetMediaUnitDetailsBinding.inflate(inflater, container, false)
        val mediaUnitDto: MediaUnitDto? = arguments?.let {
            BundleCompat.getParcelable(it, BUNDLE_MEDIA_UNIT_ADAPTER, MediaUnitDto::class.java)
        }
        val mediaUnit = mediaUnitDto?.toMediaUnit()
        binding.mediaUnit = mediaUnit

        val thumbnailUrl = mediaUnit?.thumbnail?.smallOrHigher() ?: arguments?.getString(BUNDLE_THUMBNAIL)
        binding.ivThumbnail.load(thumbnailUrl)

        binding.ivThumbnail.setOnClickListener {
            mediaUnit?.thumbnail?.originalOrDown()?.let { imageUrl ->
                val title = mediaUnit.title(requireContext())
                openPhotoViewActivity(imageUrl, title, thumbnailUrl)
            }
        }

        return binding.root
    }

    companion object {
        const val TAG = "media_unit_details_bottom_sheet"
        const val BUNDLE_MEDIA_UNIT_ADAPTER = "media_unit_adapter_bundle_key"
        const val BUNDLE_THUMBNAIL = "thumbnail_bundle_key"
    }
}