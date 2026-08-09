package io.github.drumber.kitsune.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/** Read-only profile screen for viewing another user's profile. */
class UserProfileFragment : BaseProfileFragment() {

    private val args: UserProfileFragmentArgs by navArgs()

    override val viewModel: UserProfileViewModel by viewModel {
        parametersOf(args.userIdOrSlug)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Self-only UI is not used in the read-only profile view.
            nsvNotLoggedIn.isVisible = false

            // Show the provided name immediately, before the full profile loads.
            toolbar.title = args.userName ?: getString(R.string.nav_profile)
            collapsingToolbar.title = args.userName ?: getString(R.string.nav_profile)

            fabPostWall.setOnClickListener {
                val user = viewModel.getUser() ?: return@setOnClickListener
                val action = UserProfileFragmentDirections.actionGlobalCreatePostFragment(
                    targetUserId = user.id,
                    targetUserName = user.name
                )
                findNavController().navigateSafe(R.id.user_profile_fragment, action)
            }
        }
    }

    override fun onUserModelChanged(user: User?) {
        super.onUserModelChanged(user)

        // Flush the data binding synchronously so our manual title/subtitle below
        // win over the shared layout's "user.name ?? not_logged_in" expression.
        binding.executePendingBindings()
        val displayName = user?.name ?: user?.slug ?: args.userName
        ?: getString(R.string.nav_profile)
        // The CollapsingToolbarLayout draws its own title, so set it there directly.
        binding.collapsingToolbar.title = displayName
        binding.toolbar.apply {
            title = displayName
            // Only show the @tag as subtitle when we also have a distinct display name.
            subtitle = user?.slug?.takeIf { !user.name.isNullOrBlank() }?.let { "@$it" }
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.apply {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24)
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            // Hide self-only menu items; keep only the share action.
            toolbar.menu.apply {
                findItem(R.id.menu_edit_profile)?.isVisible = false
                findItem(R.id.menu_settings)?.isVisible = false
                findItem(R.id.menu_log_out)?.isVisible = false
                findItem(R.id.menu_share_profile_url)?.isVisible = true
            }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_profile_url -> {
                        val user = viewModel.getUser()
                        val profileId = user?.slug ?: user?.id ?: args.userIdOrSlug
                        val url = io.github.drumber.kitsune.constants.Kitsu.USER_URL_PREFIX + profileId
                        startUrlShareIntent(url)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun createProfileViewPagerAdapter(userId: String): ProfileViewPagerAdapter {
        return ProfileViewPagerAdapter(userId, false, R.id.user_profile_fragment, this)
    }
}
