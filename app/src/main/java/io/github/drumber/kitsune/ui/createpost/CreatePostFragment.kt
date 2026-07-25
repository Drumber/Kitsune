package io.github.drumber.kitsune.ui.createpost

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePostFragment : Fragment() {

    private val viewModel: CreatePostViewModel by viewModel()

    private val args: CreatePostFragmentArgs by navArgs()

    /** True when at least one image failed base64 encoding; observed by the Compose screen. */
    private var imageEncodingFailed by mutableStateOf(false)

    private val pickImages =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(CreatePostViewModel.MAX_IMAGES)
        ) { uris -> onImageUrisSelected(uris) }

    private val legacyGetContents =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            onImageUrisSelected(uris)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        CreatePostScreen(
            uiState = uiState,
            events = viewModel.events,
            imageEncodingError = imageEncodingFailed,
            onImageEncodingErrorShown = { imageEncodingFailed = false },
            onContentChange = viewModel::setContent,
            onSpoilerToggle = viewModel::setSpoiler,
            onNsfwToggle = viewModel::setNsfw,
            onTagMediaClick = {
                MediaPickerBottomSheet().show(childFragmentManager, MediaPickerBottomSheet.TAG)
            },
            onTagUnitClick = {
                viewModel.uiState.value.media?.let { media ->
                    UnitPickerBottomSheet().apply {
                        arguments = bundleOf(
                            UnitPickerBottomSheet.BUNDLE_MEDIA_ID to media.id,
                            UnitPickerBottomSheet.BUNDLE_IS_ANIME to media.isAnime,
                            UnitPickerBottomSheet.BUNDLE_POSTER to media.posterUrl
                        )
                    }.show(childFragmentManager, UnitPickerBottomSheet.TAG)
                }
            },
            onClearMedia = viewModel::clearMedia,
            onClearUnit = viewModel::clearUnit,
            onAddImageClick = ::openImagePicker,
            onRemoveImage = viewModel::removeImage,
            onPublish = viewModel::publish,
            onNavigateUp = { findNavController().navigateUp() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.editPost?.let { post -> viewModel.initFromPost(post) }
        if (args.editPost == null) {
            args.targetUserId?.let { viewModel.setWallTarget(it, args.targetUserName) }
            args.targetGroupId?.let { viewModel.setGroupTarget(it, args.targetGroupName) }
        }

        childFragmentManager.setFragmentResultListener(
            MediaPickerBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val id = bundle.getString(MediaPickerBottomSheet.BUNDLE_MEDIA_ID)
                ?: return@setFragmentResultListener
            viewModel.setMedia(
                CreatePostViewModel.SelectedMedia(
                    id = id,
                    title = bundle.getString(MediaPickerBottomSheet.BUNDLE_MEDIA_TITLE).orEmpty(),
                    posterUrl = bundle.getString(MediaPickerBottomSheet.BUNDLE_MEDIA_POSTER),
                    isAnime = bundle.getBoolean(MediaPickerBottomSheet.BUNDLE_MEDIA_IS_ANIME)
                )
            )
        }

        childFragmentManager.setFragmentResultListener(
            UnitPickerBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val id = bundle.getString(UnitPickerBottomSheet.BUNDLE_UNIT_ID)
                ?: return@setFragmentResultListener
            viewModel.setUnit(
                CreatePostViewModel.SelectedUnit(
                    id = id,
                    number = bundle.getInt(UnitPickerBottomSheet.BUNDLE_UNIT_NUMBER),
                    title = bundle.getString(UnitPickerBottomSheet.BUNDLE_UNIT_TITLE).orEmpty(),
                    isEpisode = bundle.getBoolean(UnitPickerBottomSheet.BUNDLE_UNIT_IS_EPISODE)
                )
            )
        }
    }

    private fun openImagePicker() {
        if (!KitsunePref.forceLegacyImagePicker
            && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(requireContext())
        ) {
            pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            legacyGetContents.launch("image/*")
        }
    }

    private fun onImageUrisSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewLifecycleOwner.lifecycleScope.launch {
            var encodingFailed = false
            for (uri in uris) {
                if (viewModel.uiState.value.images.size >= CreatePostViewModel.MAX_IMAGES) break
                val dataUri = getBase64ImageFrom(uri)
                if (dataUri == null) {
                    encodingFailed = true
                    continue
                }
                viewModel.addImage(uri.toString(), dataUri)
            }
            if (encodingFailed) {
                imageEncodingFailed = true
            }
        }
    }

    private suspend fun getBase64ImageFrom(uri: Uri): String? = withContext(Dispatchers.IO) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
            ?: return@withContext null

        val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"

        try {
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
