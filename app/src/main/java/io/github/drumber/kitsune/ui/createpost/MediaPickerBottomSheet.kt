package io.github.drumber.kitsune.ui.createpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.paging.compose.collectAsLazyPagingItems
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.connectView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaPickerBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: MediaPickerViewModel by viewModel()
    private val connectionHandler = ConnectionHandler()
    private val composeSearchBoxView = ComposeSearchBoxView()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val status by viewModel.status.collectAsStateWithLifecycle()
        val searchItems = viewModel.searchResultSource.collectAsLazyPagingItems()
        val query by composeSearchBoxView.query

        MediaPickerScreen(
            searchQuery = query,
            onSearchQueryChange = composeSearchBoxView::change,
            searchItems = searchItems,
            status = status,
            onMediaClick = { media ->
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(
                        BUNDLE_MEDIA_ID to media.id,
                        BUNDLE_MEDIA_TITLE to media.title,
                        BUNDLE_MEDIA_POSTER to media.posterImageUrl,
                        BUNDLE_MEDIA_IS_ANIME to (media is Anime)
                    )
                )
                dismiss()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.searchBox.observe(viewLifecycleOwner) { searchBox ->
            connectionHandler.clear()
            connectionHandler += searchBox.connectView(composeSearchBoxView)
        }
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
