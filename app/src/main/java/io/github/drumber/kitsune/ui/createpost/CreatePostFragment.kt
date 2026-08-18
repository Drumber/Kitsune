package io.github.drumber.kitsune.ui.createpost

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.google.android.material.button.MaterialButton
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.databinding.FragmentCreatePostBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.markwon.MarkdownPreviewRenderer
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    private val binding by viewBinding(FragmentCreatePostBinding::bind)

    private val viewModel: CreatePostViewModel by viewModel()

    private val args: CreatePostFragmentArgs by navArgs()

    private val previewRenderer: MarkdownPreviewRenderer by inject()

    private var imageAdapter: PostImageThumbnailAdapter? = null

    private var publishButton: MaterialButton? = null

    private val pickImages =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(CreatePostViewModel.MAX_IMAGES)
        ) { uris ->
            onImageUrisSelected(uris)
        }

    private val legacyGetContents =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            onImageUrisSelected(uris)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.nsvContent.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        publishButton = binding.toolbar.menu.findItem(R.id.menu_publish_post)
            ?.actionView?.findViewById(R.id.btn_publish)
        publishButton?.setOnClickListener { viewModel.publish() }

        binding.etContent.doAfterTextChanged { text ->
            viewModel.setContent(text?.toString().orEmpty())
        }

        binding.buttonToggleGroupPreview.addOnButtonCheckedListener { _, id, isChecked ->
            if (id == R.id.btn_preview) {
                viewModel.setPreview(isChecked)
            }
        }

        binding.chipSpoiler.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSpoiler(isChecked)
        }
        binding.chipNsfw.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNsfw(isChecked)
        }

        setupTagging()

        imageAdapter = PostImageThumbnailAdapter { position ->
            viewModel.removeImage(position)
        }
        binding.rvImages.adapter = imageAdapter
        binding.btnAddImage.setOnClickListener { openImagePicker() }
        binding.btnAddImageLarge.setOnClickListener { openImagePicker() }

        val dragCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.START or ItemTouchHelper.END, 0
        ) {
            override fun isLongPressDragEnabled() = true

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
                imageAdapter?.onItemMove(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                imageAdapter?.let { viewModel.reorderImages(it.currentItems()) }
            }
        }
        ItemTouchHelper(dragCallback).attachToRecyclerView(binding.rvImages)

        args.editPost?.let { post ->
            if (savedInstanceState == null) {
                viewModel.initFromPost(post)
                binding.etContent.setText(post.content)
                binding.chipSpoiler.isChecked = post.spoiler
                binding.chipNsfw.isChecked = post.nsfw
            }
            binding.toolbar.setTitle(R.string.title_edit_post)
        }

        if (args.editPost == null) {
            args.targetUserId?.let { targetUserId ->
                viewModel.setWallTarget(targetUserId, args.targetUserName)
            }
            args.targetGroupId?.let { targetGroupId ->
                viewModel.setGroupTarget(targetGroupId, args.targetGroupName)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.localUser.collectLatest { user -> updateUser(user) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> renderState(state) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        CreatePostViewModel.Event.LoginRequired ->
                            showSnackbar(binding.root, R.string.comment_login_required)

                        CreatePostViewModel.Event.Published -> {
                            showSnackbar(
                                binding.root,
                                if (viewModel.uiState.value.isEditMode) R.string.post_updated
                                else R.string.post_published
                            )
                            findNavController().navigateUp()
                        }

                        CreatePostViewModel.Event.Error ->
                            showSnackbar(binding.root, R.string.comment_action_failed)
                    }
                }
            }
        }
    }

    private fun setupTagging() {
        binding.cardSearchMedia.setOnClickListener {
            MediaPickerBottomSheet().show(childFragmentManager, MediaPickerBottomSheet.TAG)
        }
        binding.chipTagUnit.setOnClickListener {
            val media = viewModel.uiState.value.media ?: return@setOnClickListener
            UnitPickerBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(UnitPickerBottomSheet.BUNDLE_MEDIA_ID, media.id)
                    putBoolean(UnitPickerBottomSheet.BUNDLE_IS_ANIME, media.isAnime)
                    putString(UnitPickerBottomSheet.BUNDLE_POSTER, media.posterUrl)
                }
            }.show(childFragmentManager, UnitPickerBottomSheet.TAG)
        }
        binding.btnRemoveMedia.setOnClickListener { viewModel.clearMedia() }
        binding.chipTagUnit.setOnCloseIconClickListener { viewModel.clearUnit() }

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
                showSnackbar(binding.root, R.string.comment_action_failed)
            }
        }
    }

    private suspend fun getBase64ImageFrom(uri: Uri): String? = withContext(Dispatchers.IO) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
            ?: return@withContext null

        // get mime type from image (default to jpeg)
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

    private fun updateUser(user: LocalUser?) {
        binding.ivAuthorAvatar.load(user?.avatar?.originalOrDown()) {
            placeholder(R.drawable.profile_picture_placeholder)
            error(R.drawable.profile_picture_placeholder)
            fallback(R.drawable.profile_picture_placeholder)
        }

        binding.tvAuthor.text = user?.name ?: getString(R.string.feed_unknown_user)
    }

    private fun renderState(state: CreatePostViewModel.UiState) {
        publishButton?.isEnabled = state.canPublish

        if (state.wallTargetName != null) {
            binding.toolbar.subtitle =
                getString(R.string.create_post_wall_hint, state.wallTargetName)
        } else if (state.groupTargetName != null) {
            binding.toolbar.subtitle =
                getString(R.string.create_post_group_hint, state.groupTargetName)
        }

        val hasImages = state.images.isNotEmpty()
        binding.layoutEmptyImagesPlaceholder.isVisible = !hasImages
        binding.btnAddImage.isVisible = hasImages
        binding.btnAddImage.isEnabled = state.images.size < CreatePostViewModel.MAX_IMAGES
        binding.rvImages.isVisible = hasImages
        imageAdapter?.submitItems(state.images.map { it.uri })

        val hasContent = state.content.isNotBlank()
        val isPreview = state.isPreview
        binding.tilContent.isVisible = !isPreview
        binding.tvPreview.isVisible = isPreview && hasContent
        binding.tvPreviewPlaceholder.isVisible = isPreview && !hasContent
        if (isPreview && hasContent) {
            previewRenderer.render(binding.tvPreview, state.content)
        }

        val media = state.media
        binding.layoutSelectedMedia.isVisible = media != null
        if (media != null) {
            binding.tvMediaTitle.text = media.title
            binding.ivMediaPoster.load(media.posterUrl)
        }

        val unit = state.unit
        binding.chipTagUnit.isCloseIconVisible = unit != null
        binding.chipTagUnit.text = when (unit != null) {
            true -> if (unit.isEpisode)
                getString(R.string.unit_episode, unit.number)
            else
                getString(R.string.unit_chapter, unit.number)

            false -> getString(R.string.action_tag_unit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvImages.adapter = null
        imageAdapter = null
        publishButton = null
    }
}
