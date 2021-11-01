package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.databinding.SheetManageLibraryBinding

class ManageLibraryBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SheetManageLibraryBinding.inflate(inflater, container, false)
        binding.apply {
            instance = this@ManageLibraryBottomSheet
            title = arguments?.getString(BUNDLE_TITLE)
        }
        return binding.root
    }

    fun onStatusClicked(status: Status) {
        setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_STATUS to status))
        dismiss()
    }

    companion object {
        const val TAG = "manage_library_bottom_sheet"
        const val BUNDLE_TITLE = "title_bundle_key"
        const val BUNDLE_STATUS = "status_bundle_key"
        const val REQUEST_KEY = "status_request_key"
    }

}