package io.github.drumber.kitsune.ui.createpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.databinding.SheetUnitPickerBinding
import io.github.drumber.kitsune.ui.adapter.paging.MediaUnitPagingAdapter
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class UnitPickerBottomSheet : BottomSheetDialogFragment(),
    MediaUnitPagingAdapter.MediaUnitActionListener {

    private val binding by viewBinding(SheetUnitPickerBinding::bind)

    private val viewModel: UnitPickerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return SheetUnitPickerBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mediaId = arguments?.getString(BUNDLE_MEDIA_ID) ?: run {
            dismiss()
            return
        }
        val isAnime = arguments?.getBoolean(BUNDLE_IS_ANIME) ?: true
        val posterUrl = arguments?.getString(BUNDLE_POSTER)

        val adapter = MediaUnitPagingAdapter(
            glide = Glide.with(this),
            posterUrl = posterUrl,
            enableWatchedCheckbox = false,
            listener = this
        )
        binding.rvUnits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUnits.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.unitPager(mediaId, isAnime).collectLatest { adapter.submitData(it) }
            }
        }
    }

    override fun onMediaUnitClicked(mediaUnit: MediaUnit) {
        val id = mediaUnit.id ?: return
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                BUNDLE_UNIT_ID to id,
                BUNDLE_UNIT_NUMBER to (mediaUnit.number ?: 0),
                BUNDLE_UNIT_TITLE to mediaUnit.title(requireContext()),
                BUNDLE_UNIT_IS_EPISODE to (mediaUnit is Episode)
            )
        )
        dismiss()
    }

    override fun onWatchStateChanged(mediaUnit: MediaUnit, isWatched: Boolean) {
        // Not used in the picker.
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
