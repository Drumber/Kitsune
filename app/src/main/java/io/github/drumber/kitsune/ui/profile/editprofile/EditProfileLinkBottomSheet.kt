package io.github.drumber.kitsune.ui.profile.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.ui.compose.composeView

class EditProfileLinkBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileLinkEntry = arguments?.let { bundle ->
            BundleCompat.getParcelable(
                bundle,
                BUNDLE_PROFILE_LINK_ENTRY,
                ProfileLinkEntry::class.java
            )
        } ?: return composeView {}

        val isCreatingNew = arguments?.getBoolean(BUNDLE_IS_CREATING_NEW) == true

        return composeView {
            EditProfileLinkScreen(
                profileLinkEntry = profileLinkEntry,
                isCreatingNew = isCreatingNew,
                onConfirm = { url ->
                    setFragmentResult(
                        PROFILE_SUCCESS_REQUEST_KEY,
                        bundleOf(BUNDLE_PROFILE_LINK_ENTRY to profileLinkEntry.copy(url = url))
                    )
                    dismiss()
                },
                onDelete = {
                    setFragmentResult(
                        PROFILE_DELETE_REQUEST_KEY,
                        bundleOf(BUNDLE_PROFILE_LINK_ENTRY to profileLinkEntry)
                    )
                    dismiss()
                },
                onCancel = { dismiss() }
            )
        }
    }

    companion object {
        const val TAG = "edit_profile_link_bottom_sheet"
        const val BUNDLE_IS_CREATING_NEW = "is_creating_new_bundle_key"
        const val BUNDLE_PROFILE_LINK_ENTRY = "profile_link_entry_bundle_key"
        const val PROFILE_SUCCESS_REQUEST_KEY = "edit_profile_link_success_request_key"
        const val PROFILE_DELETE_REQUEST_KEY = "edit_profile_link_delete_request_key"
    }
}
