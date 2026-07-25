package io.github.drumber.kitsune.ui.profile.editprofile

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import com.google.android.material.datepicker.MaterialDatePicker
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseDialogFragment
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.toDate
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileFragment : BaseDialogFragment(0) {

    private val viewModel: EditProfileViewModel by viewModel()

    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var legacyGetContent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            onImageUriSelected(uri)
        }

        legacyGetContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onImageUriSelected(uri)
        }
    }

    override fun onStart() {
        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        EditProfileScreen(
            viewModel = viewModel,
            onDismiss = { dismiss() },
            onAvatarClick = { openImagePicker(ImagePickerType.AVATAR) },
            onCoverClick = { openImagePicker(ImagePickerType.COVER) },
            onBirthdayClick = { selectedDate ->
                openDatePicker(selectedDate, getString(R.string.profile_data_birthday)) { date ->
                    val dateString = date.toDate().formatDate("yyyy-MM-dd")
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(birthday = dateString)
                    )
                }
            },
            onAddProfileLink = { openSelectProfileLinkSiteBottomSheet() },
            onEditProfileLink = { entry -> openEditProfileLinkBottomSheet(entry, false) },
            onSaveClick = { viewModel.updateUserProfile(createUserImageUpload()) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.hasUser()) {
            Toast.makeText(requireContext(), R.string.error_invalid_user, Toast.LENGTH_LONG).show()
            dismiss()
            return
        }

        childFragmentManager.setFragmentResultListener(
            SelectProfileLinkSiteBottomSheet.PROFILE_SITE_SELECTED_REQUEST_KEY,
            this
        ) { _, bundle ->
            val linkSite = BundleCompat.getParcelable(
                bundle,
                SelectProfileLinkSiteBottomSheet.BUNDLE_PROFILE_LINK_SITE,
                ProfileLinkSite::class.java
            ) ?: return@setFragmentResultListener

            openEditProfileLinkBottomSheet(ProfileLinkEntry(null, "", linkSite), true)
        }

        childFragmentManager.setFragmentResultListener(
            EditProfileLinkBottomSheet.PROFILE_SUCCESS_REQUEST_KEY,
            this
        ) { _, bundle ->
            val profileLinkEntry = BundleCompat.getParcelable(
                bundle,
                EditProfileLinkBottomSheet.BUNDLE_PROFILE_LINK_ENTRY,
                ProfileLinkEntry::class.java
            ) ?: return@setFragmentResultListener

            viewModel.acceptProfileLinkAction(ProfileLinkAction.Edit(profileLinkEntry))
        }

        childFragmentManager.setFragmentResultListener(
            EditProfileLinkBottomSheet.PROFILE_DELETE_REQUEST_KEY,
            this
        ) { _, bundle ->
            val profileLinkEntry = BundleCompat.getParcelable(
                bundle,
                EditProfileLinkBottomSheet.BUNDLE_PROFILE_LINK_ENTRY,
                ProfileLinkEntry::class.java
            ) ?: return@setFragmentResultListener

            viewModel.acceptProfileLinkAction(ProfileLinkAction.Delete(profileLinkEntry))
        }
    }

    private fun openSelectProfileLinkSiteBottomSheet() {
        SelectProfileLinkSiteBottomSheet()
            .show(childFragmentManager, SelectProfileLinkSiteBottomSheet.TAG)
    }

    private fun openEditProfileLinkBottomSheet(
        profileLinkEntry: ProfileLinkEntry,
        isCreatingNew: Boolean
    ) {
        EditProfileLinkBottomSheet().apply {
            arguments = bundleOf(
                EditProfileLinkBottomSheet.BUNDLE_IS_CREATING_NEW to isCreatingNew,
                EditProfileLinkBottomSheet.BUNDLE_PROFILE_LINK_ENTRY to profileLinkEntry
            )
        }.show(childFragmentManager, EditProfileLinkBottomSheet.TAG)
    }

    private fun openDatePicker(selectedDate: Long, title: String, action: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selectedDate)
            .build()
        datePicker.addOnPositiveButtonClickListener(action)
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun openImagePicker(type: ImagePickerType) {
        viewModel.currentImagePickerType = type

        if (!KitsunePref.forceLegacyImagePicker
            && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(requireContext())
        ) {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            legacyGetContent.launch("image/*")
        }
    }

    private fun onImageUriSelected(uri: Uri?) {
        if (uri != null) {
            val imageState = viewModel.profileImageState
            val newImageState = when (viewModel.currentImagePickerType) {
                ImagePickerType.AVATAR -> imageState.copy(selectedAvatarUri = uri)
                ImagePickerType.COVER -> imageState.copy(selectedCoverUri = uri)
                else -> imageState
            }
            viewModel.acceptProfileImageChanges(newImageState)
        }
        viewModel.currentImagePickerType = null
    }

    private fun createUserImageUpload(): ProfileImageContainer? {
        val imageState = viewModel.profileImageState
        val avatarUri = imageState.selectedAvatarUri
        val coverUri = imageState.selectedCoverUri
        if (avatarUri == null && coverUri == null) return null

        val profileImages = ProfileImageContainer(
            avatar = avatarUri?.let { getBase64ImageFrom(it) },
            coverImage = coverUri?.let { getBase64ImageFrom(it) }
        )
        if (profileImages.avatar == null && profileImages.coverImage == null) return null
        return profileImages
    }

    private fun getBase64ImageFrom(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
        return try {
            inputStream.use { stream ->
                val bytes = stream.readBytes()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            }.let { base64 ->
                "data:$mimeType;base64,$base64"
            }
        } catch (e: Exception) {
            logE("Error while encoding image to Base64 from uri: $uri", e)
            null
        }
    }
}
