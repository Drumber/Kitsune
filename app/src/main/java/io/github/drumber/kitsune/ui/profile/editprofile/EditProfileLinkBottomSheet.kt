package io.github.drumber.kitsune.ui.profile.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.databinding.SheetEditProfileLinkBinding
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId

class EditProfileLinkBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetEditProfileLinkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetEditProfileLinkBinding.inflate(inflater, container, false)
        binding.isCreatingNew = isCreatingNew()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileLinkEntry = arguments?.let { bundle ->
            BundleCompat.getParcelable(
                bundle,
                BUNDLE_PROFILE_LINK_ENTRY,
                ProfileLinkEntry::class.java
            )
        } ?: return

        binding.apply {
            profileLinkEntry.site.name?.let { siteName ->
                ivLogo.setImageResource(getProfileSiteLogoResourceId(siteName))
                tvSiteName.text = siteName
            }

            fieldUrl.editText?.setText(profileLinkEntry.url)
            fieldUrl.editText?.doOnTextChanged { text, _, _, _ ->
                btnConfirm.isEnabled = !text.isNullOrBlank()
            }

            btnDelete.setOnClickListener { view ->
                setFragmentResult(
                    PROFILE_DELETE_REQUEST_KEY,
                    bundleOf(BUNDLE_PROFILE_LINK_ENTRY to profileLinkEntry)
                )
                dismiss()
            }
            btnConfirm.isEnabled = fieldUrl.editText?.text?.isNotBlank() == true
            btnCancel.setOnClickListener { dismiss() }
            btnConfirm.setOnClickListener {
                val text = fieldUrl.editText?.text?.toString()
                if (text.isNullOrBlank()) return@setOnClickListener

                val editedProfileLinkEntry = profileLinkEntry.copy(url = text)
                setFragmentResult(
                    PROFILE_SUCCESS_REQUEST_KEY,
                    bundleOf(BUNDLE_PROFILE_LINK_ENTRY to editedProfileLinkEntry)
                )
                dismiss()
            }
        }
    }

    private fun isCreatingNew() = arguments?.getBoolean(BUNDLE_IS_CREATING_NEW) == true

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "edit_profile_link_bottom_sheet"
        const val BUNDLE_IS_CREATING_NEW = "is_creating_new_bundle_key"
        const val BUNDLE_PROFILE_LINK_ENTRY = "profile_link_entry_bundle_key"
        const val PROFILE_SUCCESS_REQUEST_KEY = "edit_profile_link_success_request_key"
        const val PROFILE_DELETE_REQUEST_KEY = "edit_profile_link_delete_request_key"
    }

}