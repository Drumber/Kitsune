package io.github.drumber.kitsune.ui.details.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.MediaUnitDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaUnit
import io.github.drumber.kitsune.data.presentation.extension.title
import io.github.drumber.kitsune.databinding.SheetMediaUnitDetailsBinding
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity

class MediaUnitDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetMediaUnitDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetMediaUnitDetailsBinding.inflate(inflater, container, false)
        val mediaUnitDto: MediaUnitDto? = arguments?.getParcelable(BUNDLE_MEDIA_UNIT_ADAPTER)
        val mediaUnit = mediaUnitDto?.toMediaUnit()
        binding.mediaUnit = mediaUnit

        val thumbnailUrl = mediaUnit?.thumbnail?.smallOrHigher() ?: arguments?.getString(BUNDLE_THUMBNAIL)
        Glide.with(this)
            .load(thumbnailUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(binding.ivThumbnail)

        binding.ivThumbnail.setOnClickListener {
            mediaUnit?.thumbnail?.originalOrDown()?.let { imageUrl ->
                val title = mediaUnit.title(requireContext())
                openPhotoViewActivity(imageUrl, title, thumbnailUrl)
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "media_unit_details_bottom_sheet"
        const val BUNDLE_MEDIA_UNIT_ADAPTER = "media_unit_adapter_bundle_key"
        const val BUNDLE_THUMBNAIL = "thumbnail_bundle_key"
    }
}