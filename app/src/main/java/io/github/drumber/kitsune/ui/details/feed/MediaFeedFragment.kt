package io.github.drumber.kitsune.ui.details.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.webview.WebViewFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFeedFragment : Fragment(R.layout.fragment_media_feed),
    NavigationBarView.OnItemReselectedListener {

    private val args: MediaFeedFragmentArgs by navArgs()

    private val viewModel: MediaFeedViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val items = viewModel.dataSource.collectAsLazyPagingItems()
        MediaFeedScreen(
            title = getString(R.string.title_posts),
            items = items,
            onNavigateUp = { findNavController().navigateUp() },
            onPostClick = { post -> navigateToPost(post) },
            onAuthorClick = { userId -> navigateToUser(userId) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMedia(args.mediaId, args.isAnime)
    }

    private fun navigateToPost(post: Post) {
        val url = "${Kitsu.BASE_URL}/posts/${post.id}"
        val action = WebViewFragmentDirections.actionGlobalWebViewFragment(url)
        findNavController().navigateSafe(R.id.media_feed_fragment, action)
    }

    private fun navigateToUser(userId: String) {
        val action = io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.media_feed_fragment, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        findNavController().navigateUp()
    }
}
