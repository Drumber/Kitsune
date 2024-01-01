package io.github.drumber.kitsune.ui.profile.editprofile

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.android.searchbox.SearchBoxViewEditText
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.search.helper.deserialize
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.search.SearchView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentEditProfileBinding
import io.github.drumber.kitsune.databinding.ItemProfileSiteChipBinding
import io.github.drumber.kitsune.domain.mapper.toCharacter
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.CharacterSearchResult
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseDialogFragment
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.parseDate
import io.github.drumber.kitsune.util.toDate
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileFragment : BaseDialogFragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModel()

    private val connectionHandler = ConnectionHandler()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.nestedScrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        if (!viewModel.hasUser()) {
            Toast.makeText(requireContext(), R.string.error_invalid_user, Toast.LENGTH_LONG).show()
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        binding.apply {
            toolbar.setNavigationOnClickListener { dismiss() }

            cardAvatar.setOnClickListener {
                openImagePicker(ImagePickerType.AVATAR)
            }

            cardCover.setOnClickListener {
                openImagePicker(ImagePickerType.COVER)
            }

            chipAddProfileLink.setOnClickListener { openSelectProfileLinkSiteBottomSheet() }

            fieldLocation.editText?.apply {
                setText(viewModel.profileState.location)
                doAfterTextChanged {
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(
                            location = it?.trim().toString()
                        )
                    )
                }
            }

            fieldBirthday.editText?.setOnClickListener {
                val selectedDate = viewModel.profileState.birthday.parseDate()?.time
                    ?: MaterialDatePicker.todayInUtcMilliseconds()

                openDatePicker(selectedDate, getString(R.string.profile_data_birthday)) { date ->
                    val dateString = date.toDate().formatDate("yyyy-MM-dd")
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(birthday = dateString)
                    )
                }
            }
            fieldBirthday.setEndIconOnClickListener {
                if (viewModel.profileState.birthday.isEmpty()) {
                    fieldBirthday.editText?.performClick()
                } else {
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(birthday = "")
                    )
                }
            }

            (menuGender.editText as? MaterialAutoCompleteTextView)?.apply {
                val genderItems = arrayOf(
                    R.string.profile_data_private,
                    R.string.profile_gender_male,
                    R.string.profile_gender_female,
                    R.string.profile_gender_custom
                ).map { getString(it) }.toTypedArray()
                setSimpleItems(genderItems)
                setText(
                    DataUtil.getGenderString(viewModel.profileState.gender, requireContext()),
                    false
                )

                setOnItemClickListener { _, _, position, _ ->
                    val gender = when (position) {
                        0 -> "secret"
                        1 -> "male"
                        2 -> "female"
                        else -> "custom"
                    }
                    val customGender =
                        if (gender != "custom") "" else viewModel.profileState.customGender
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(
                            gender = gender,
                            customGender = customGender
                        )
                    )
                    binding.fieldCustomGender.editText?.setText(customGender)
                    if (gender == "custom") {
                        postDelayed(100) {
                            binding.fieldCustomGender.editText?.requestFocus()
                        }
                    }
                }
            }

            fieldCustomGender.editText?.setText(viewModel.profileState.customGender)
            fieldCustomGender.editText?.doAfterTextChanged {
                viewModel.acceptProfileChanges(
                    viewModel.profileState.copy(customGender = it?.trim().toString())
                )
            }

            (menuWaifu.editText as? MaterialAutoCompleteTextView)?.apply {
                val waifuItems = arrayOf(
                    "",
                    getString(R.string.profile_data_waifu),
                    getString(R.string.profile_data_husbando)
                )
                setSimpleItems(waifuItems)
                setText(viewModel.profileState.waifuOrHusbando, false)

                setOnItemClickListener { _, _, position, _ ->
                    val waifuOrHusbando = waifuItems[position]
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(waifuOrHusbando = waifuOrHusbando)
                    )
                }
            }

            fieldSearchWaifu.editText?.setOnClickListener {
                viewModel.initSearchClient()
                characterSearchView.clearText()
                characterSearchView.show()
            }
            fieldSearchWaifu.setEndIconOnClickListener {
                if (viewModel.profileState.character != null) {
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(character = null)
                    )
                } else {
                    fieldSearchWaifu.editText?.performClick()
                }
            }

            fieldBio.editText?.apply {
                setText(viewModel.profileState.about)
                doAfterTextChanged {
                    viewModel.acceptProfileChanges(
                        viewModel.profileState.copy(about = it?.trim().toString())
                    )
                }
            }

            btnUpdateProfile.setOnClickListener {
                viewModel.updateUserProfile(createUserImageUpload())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileStateFlow.collectLatest { profileState ->
                binding.apply {
                    fieldBirthday.editText?.setText(
                        profileState.birthday.parseDate()?.formatDate()
                    )
                    fieldBirthday.setEndIconDrawable(
                        if (profileState.birthday.isEmpty()) {
                            R.drawable.ic_calendar_month_24
                        } else {
                            R.drawable.ic_close_24
                        }
                    )

                    fieldCustomGender.isVisible = profileState.gender == "custom"

                    fieldSearchWaifu.apply {
                        isVisible = profileState.waifuOrHusbando.isNotBlank()
                        editText?.setText(profileState.character?.name)
                        setEndIconDrawable(
                            if (profileState.character == null) {
                                R.drawable.ic_search_24
                            } else {
                                R.drawable.ic_heart_broken_24
                            }
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileImageStateFlow.collectLatest { profileImageState ->
                val avatarImage = profileImageState.selectedAvatarUri
                    ?: profileImageState.currentAvatarUrl
                Glide.with(this@EditProfileFragment)
                    .load(avatarImage)
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .into(binding.ivAvatar)

                val coverImage = profileImageState.selectedCoverUri
                    ?: profileImageState.currentCoverUrl
                Glide.with(this@EditProfileFragment)
                    .load(coverImage)
                    .placeholder(R.drawable.cover_placeholder)
                    .into(binding.ivCover)

                binding.ivCoverAddImage.isVisible = coverImage == null
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileLinkEntriesFlow.collectLatest { profileLinks ->
                val indexOfAddChip = binding.chipGroupProfileLinks
                    .indexOfChild(binding.chipAddProfileLink)
                if (indexOfAddChip != -1) {
                    binding.chipGroupProfileLinks.removeViews(0, indexOfAddChip)
                }

                profileLinks.sortedByDescending { it.site.id?.toIntOrNull() }.forEach { link ->
                    val chipBinding = ItemProfileSiteChipBinding.inflate(
                        layoutInflater,
                        binding.chipGroupProfileLinks,
                        false
                    )
                    val chip = chipBinding.root
                    chip.text = link.site.name
                    chip.setChipIconResource(getProfileSiteLogoResourceId(link.site.name))
                    chip.setOnClickListener {
                        openEditProfileLinkBottomSheet(link, false)
                    }
                    binding.chipGroupProfileLinks.addView(chip, 0)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.canUpdateProfileFlow.collectLatest { canUpdate ->
                binding.btnUpdateProfile.isEnabled = canUpdate
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadingStateFlow.collectLatest { loadingState ->
                binding.layoutLoading.isVisible = loadingState is LoadingState.Loading

                if (loadingState is LoadingState.Error && !loadingState.isConsumed) {
                    loadingState.isConsumed = true

                    if (loadingState.exception is ProfileUpdateException) {
                        showErrorToUser(loadingState.exception)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_user_update_failed,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else if (loadingState is LoadingState.Success) {
                    dismiss()
                }
            }
        }

        initSearchView()
    }

    private fun initSearchView() {
        val adapter = CharacterSearchResultAdapter {
            viewModel.acceptProfileChanges(viewModel.profileState.copy(character = it.toCharacter()))
            binding.characterSearchView.hide()
        }

        binding.rvCharacterResults.apply {
            initPaddingWindowInsetsListener(
                left = true,
                right = true,
                bottom = true,
                consume = false
            )
            this.adapter = adapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                binding.rvCharacterResults.scrollToPosition(0)
            }
        })

        binding.characterSearchView.editText.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                adapter.setHits(emptyList())
            }
        }

        val backPressedCallback = (dialog as ComponentDialog).onBackPressedDispatcher
            .addCallback(this, false) {
                binding.characterSearchView.hide()
                isEnabled = false
            }

        binding.characterSearchView.addTransitionListener { _, _, newState ->
            backPressedCallback.isEnabled = newState == SearchView.TransitionState.SHOWN ||
                    newState == SearchView.TransitionState.SHOWING
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchBoxConnectorFlow.collectLatest { searchBox ->
                connectionHandler.clear()
                val searchBoxView = SearchBoxViewEditText(binding.characterSearchView.editText)
                connectionHandler += searchBox.connectView(searchBoxView)
                connectionHandler += searchBox.searcher.connectHitsView(adapter) { response ->
                    response.hits.deserialize(CharacterSearchResult.serializer())
                }
            }
        }
    }

    private fun openSelectProfileLinkSiteBottomSheet() {
        val bottomSheet = SelectProfileLinkSiteBottomSheet()
        bottomSheet.show(childFragmentManager, SelectProfileLinkSiteBottomSheet.TAG)
    }

    private fun openEditProfileLinkBottomSheet(
        profileLinkEntry: ProfileLinkEntry,
        isCreatingNew: Boolean
    ) {
        val bottomSheet = EditProfileLinkBottomSheet().apply {
            arguments = bundleOf(
                EditProfileLinkBottomSheet.BUNDLE_IS_CREATING_NEW to isCreatingNew,
                EditProfileLinkBottomSheet.BUNDLE_PROFILE_LINK_ENTRY to profileLinkEntry
            )
        }
        bottomSheet.show(childFragmentManager, EditProfileLinkBottomSheet.TAG)
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
        if (avatarUri == null && coverUri == null) {
            return null
        }

        val profileImages = ProfileImageContainer(
            avatar = avatarUri?.let { getBase64ImageFrom(it) },
            coverImage = coverUri?.let { getBase64ImageFrom(it) }
        )

        if (profileImages.avatar == null && profileImages.coverImage == null) {
            return null
        }
        return profileImages
    }

    private fun getBase64ImageFrom(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null

        // get mime type from image (default to jpeg)
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

    private fun showErrorToUser(profileUpdateException: ProfileUpdateException) {
        val message = when (profileUpdateException) {
            is ProfileUpdateException.ProfileDataError -> when (profileUpdateException.type) {
                ProfileDataErrorType.UpdateProfile -> getString(R.string.error_user_update_failed)
                ProfileDataErrorType.DeleteWaifu -> getString(R.string.error_user_delete_waifu_failed)
            }

            is ProfileUpdateException.ProfileImageError -> getString(R.string.error_user_update_image_failed)

            is ProfileUpdateException.ProfileLinkError -> {
                val siteName = profileUpdateException.profileLinkEntry.site.name
                    ?: viewModel.profileLinkSites
                        ?.find { it.id == profileUpdateException.profileLinkEntry.site.id }?.name
                    ?: profileUpdateException.profileLinkEntry.url

                when (profileUpdateException.operation) {
                    ProfileLinkOperation.Create -> getString(
                        R.string.error_user_create_profile_link_failed,
                        siteName
                    )

                    ProfileLinkOperation.Update -> getString(
                        R.string.error_user_update_profile_link_failed,
                        siteName
                    )

                    ProfileLinkOperation.Delete -> getString(
                        R.string.error_user_delete_profile_link_failed,
                        siteName
                    )
                }
            }
        }

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        connectionHandler.clear()
    }
}