package io.github.drumber.kitsune.ui.groupdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.feed.FeedListFragment
import io.github.drumber.kitsune.ui.feed.FeedType
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GroupDetailFragment : Fragment() {

    private val args: GroupDetailFragmentArgs by navArgs()

    private val viewModel: GroupDetailViewModel by viewModel {
        parametersOf(args.groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        GroupDetailContent()
    }

    @Composable
    private fun GroupDetailContent() {
        val group by viewModel.group.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val membershipState by viewModel.membershipState.collectAsStateWithLifecycle()

        GroupDetailScreen(
            group = group,
            isLoading = isLoading,
            membershipState = membershipState,
            events = viewModel.events,
            isMemberDefault = membershipState.isMember,
            onNavigateUp = { findNavController().navigateUp() },
            onJoinLeave = { viewModel.toggleMembership() },
            onOpenCover = {
                group?.coverImageUrl?.let { url ->
                    openPhotoViewActivity(url, group?.name)
                }
            },
            onNavigateToCreatePost = {
                val action = GroupDetailFragmentDirections.actionGlobalCreatePostFragment(
                    targetGroupId = args.groupId,
                    targetGroupName = viewModel.group.value?.name
                )
                findNavController().navigateSafe(R.id.group_detail_fragment, action)
            },
            feedContent = { GroupFeedContent() }
        )
    }

    /**
     * Embeds [FeedListFragment] inside the Compose tree using [AndroidFragment].
     *
     * [AndroidFragment] adds the child via [FragmentTransaction.add(ViewGroup, Fragment, String)]
     * which sets `mInDynamicContainer = true`. This tells the Fragment system that the container
     * is provided dynamically — it does not try to find the container by a static resource ID
     * when restoring the fragment after back-stack navigation or a config change. Fragment
     * instance state is saved via [rememberFragmentState] (backed by [rememberSaveable]) so it
     * survives both back-stack-return and process death.
     *
     * The parent [GroupDetailContent] always keeps this composable in the tree (even when the
     * About tab is selected) so the Fragment is not destroyed on tab switch.
     */
    @Composable
    private fun GroupFeedContent() {
        val groupId = args.groupId
        AndroidFragment<FeedListFragment>(
            modifier = Modifier.fillMaxSize(),
            arguments = Bundle().apply {
                putString(FeedListFragment.ARG_FEED_TYPE, FeedType.GROUP.name)
                putString(FeedListFragment.ARG_GROUP_ID, groupId)
                putInt(FeedListFragment.ARG_HOST_DEST_ID, R.id.group_detail_fragment)
            }
        )
    }
}


