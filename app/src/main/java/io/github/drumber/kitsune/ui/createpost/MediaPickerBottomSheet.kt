package io.github.drumber.kitsune.ui.createpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.algolia.instantsearch.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.connectView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.SheetMediaPickerBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.MediaSearchPagingAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaPickerBottomSheet : BottomSheetDialogFragment(), OnItemClickListener<Media> {

    private var _binding: SheetMediaPickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MediaPickerViewModel by viewModel()

    private val connectionHandler = ConnectionHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetMediaPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MediaSearchPagingAdapter(Glide.with(this), this)
        binding.rvMedia.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMedia.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultSource.collectLatest { adapter.submitData(it) }
            }
        }

        viewModel.searchBox.observe(viewLifecycleOwner) { searchBox ->
            val searchBoxView = SearchBoxViewAppCompat(binding.searchView)
            connectionHandler += searchBox.connectView(searchBoxView)
        }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.progressBar.isVisible = status == MediaPickerViewModel.Status.NotInitialized
            val message = when (status) {
                MediaPickerViewModel.Status.NotAvailable -> getString(R.string.search_provider_not_available)
                MediaPickerViewModel.Status.Error -> getString(R.string.search_provider_error)
                else -> null
            }
            binding.tvStatus.text = message
            binding.tvStatus.isVisible = message != null
        }
    }

    override fun onItemClick(view: View, item: Media) {
        val isAnime = item is Anime
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                BUNDLE_MEDIA_ID to item.id,
                BUNDLE_MEDIA_TITLE to item.title,
                BUNDLE_MEDIA_POSTER to item.posterImageUrl,
                BUNDLE_MEDIA_IS_ANIME to isAnime
            )
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connectionHandler.clear()
        _binding = null
    }

    companion object {
        const val TAG = "media_picker_bottom_sheet"
        const val REQUEST_KEY = "media_picker_request"
        const val BUNDLE_MEDIA_ID = "media_id"
        const val BUNDLE_MEDIA_TITLE = "media_title"
        const val BUNDLE_MEDIA_POSTER = "media_poster"
        const val BUNDLE_MEDIA_IS_ANIME = "media_is_anime"
    }
}
