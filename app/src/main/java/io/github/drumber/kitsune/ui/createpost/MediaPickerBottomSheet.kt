package io.github.drumber.kitsune.ui.createpost

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.algolia.instantsearch.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.connectView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.SheetMediaPickerBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.MediaSearchPagingAdapter
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaPickerBottomSheet : BottomSheetDialogFragment(), OnItemClickListener<Media> {

    private val binding by viewBinding(SheetMediaPickerBinding::bind)

    private val viewModel: MediaPickerViewModel by viewModel()

    private val connectionHandler = ConnectionHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return SheetMediaPickerBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MediaSearchPagingAdapter(this)
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

        if (savedInstanceState == null) {
            binding.searchView.post {
                if (isAdded) focusSearchView()
            }
        }
    }

    private fun focusSearchView() {
        binding.searchView.requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onItemClick(view: View, item: Media) {
        val isAnime = item is Anime
        setFragmentResult(
            REQUEST_KEY,
            Bundle().apply {
                putString(BUNDLE_MEDIA_ID, item.id)
                putString(BUNDLE_MEDIA_TITLE, item.title)
                putString(BUNDLE_MEDIA_POSTER, item.posterImageUrl)
                putBoolean(BUNDLE_MEDIA_IS_ANIME, isAnime)
            }
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connectionHandler.clear()
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
