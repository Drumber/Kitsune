package io.github.drumber.kitsune.ui.createpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.ui.compose.composeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class UnitPickerBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: UnitPickerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val mediaId = arguments?.getString(BUNDLE_MEDIA_ID) ?: return@composeView
        val isAnime = arguments?.getBoolean(BUNDLE_IS_ANIME) ?: true
        val posterUrl = arguments?.getString(BUNDLE_POSTER)

        val units = viewModel.unitPager(mediaId, isAnime).collectAsLazyPagingItems()

        UnitPickerScreen(
            units = units,
            posterUrl = posterUrl,
            onUnitClick = { unit ->
                unit.id?.let { id ->
                    setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(
                            BUNDLE_UNIT_ID to id,
                            BUNDLE_UNIT_NUMBER to (unit.number ?: 0),
                            BUNDLE_UNIT_TITLE to unit.title(requireContext()),
                            BUNDLE_UNIT_IS_EPISODE to (unit is Episode)
                        )
                    )
                    dismiss()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.getString(BUNDLE_MEDIA_ID) == null) {
            dismiss()
        }
    }

    companion object {
        const val TAG = "unit_picker_bottom_sheet"
        const val REQUEST_KEY = "unit_picker_request"
        const val BUNDLE_MEDIA_ID = "media_id"
        const val BUNDLE_IS_ANIME = "is_anime"
        const val BUNDLE_POSTER = "poster"
        const val BUNDLE_UNIT_ID = "unit_id"
        const val BUNDLE_UNIT_NUMBER = "unit_number"
        const val BUNDLE_UNIT_TITLE = "unit_title"
        const val BUNDLE_UNIT_IS_EPISODE = "unit_is_episode"
    }
}
