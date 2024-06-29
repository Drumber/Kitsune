package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus
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

    fun onStatusClicked(status: LibraryStatus) {
        setFragmentResult(STATUS_REQUEST_KEY, bundleOf(BUNDLE_STATUS to status))
        dismiss()
    }

    fun onRemoveClicked() {
        setFragmentResult(REMOVE_REQUEST_KEY, bundleOf(BUNDLE_EXISTS_IN_LIBRARY to false))
        dismiss()
    }

    fun existsInLibrary(): Boolean {
        return arguments?.getBoolean(BUNDLE_EXISTS_IN_LIBRARY) ?: false
    }

    fun isAnime() = arguments?.getBoolean(BUNDLE_IS_ANIME) == true

    companion object {
        const val TAG = "manage_library_bottom_sheet"
        const val BUNDLE_TITLE = "title_bundle_key"
        const val BUNDLE_STATUS = "status_bundle_key"
        const val BUNDLE_EXISTS_IN_LIBRARY = "status_exists_in_library"
        const val BUNDLE_IS_ANIME = "is_anime_key"
        const val STATUS_REQUEST_KEY = "status_request_key"
        const val REMOVE_REQUEST_KEY = "remove_request_key"
    }

}