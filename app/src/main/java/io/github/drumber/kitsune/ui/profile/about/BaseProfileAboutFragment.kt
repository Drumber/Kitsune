package io.github.drumber.kitsune.ui.profile.about

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.databinding.FragmentProfileAboutBinding
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.profile.BaseProfileViewModel
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseProfileAboutFragment : BaseFragment(R.layout.fragment_profile_about),
    NavigationBarView.OnItemReselectedListener {

    protected val binding by viewBinding(FragmentProfileAboutBinding::bind)

    protected abstract val viewModel: BaseProfileViewModel

    private lateinit var statsSection: ProfileStatsSection
    private lateinit var favoritesSection: ProfileFavoritesSection
    private lateinit var linksSection: ProfileLinksSection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statsSection = ProfileStatsSection(binding.viewPagerStats, binding.tabLayoutStats)
        statsSection.init(savedInstanceState == null)
        favoritesSection = ProfileFavoritesSection(
            binding = binding,
            glide = Glide.with(this),
            onMediaClick = { view, media -> onFavoriteMediaItemClicked(view, media) },
            onCharacterClick = { character -> openCharacterDetailsBottomSheet(character) }
        )
        linksSection = ProfileLinksSection(binding, layoutInflater) { onProfileLinkClicked(it) }

        binding.apply {
            swipeRefreshLayout.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            nsvContent.initPaddingWindowInsetsListener(bottom = true, consume = false)

            swipeRefreshLayout.apply {
                setAppTheme()
                setOnRefreshListener {
                    viewModel.refreshUser()
                }
            }

            val onWaifuClicked: View.OnClickListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val waifu = viewModel.getUser()?.waifu ?: return
                    openCharacterDetailsBottomSheet(waifu)
                }
            }
            layoutWaifuRow.root.setOnClickListener(onWaifuClicked)
            layoutWaifuRow.tvValue.setOnClickListener(onWaifuClicked)

            btnFollowing.setOnClickListener {
                navigateToFollowList(FollowListType.FOLLOWING)
            }
            btnFollowers.setOnClickListener {
                navigateToFollowList(FollowListType.FOLLOWERS)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                updateUser(user)
                updateProfileLinks(user?.profileLinks ?: emptyList())
                statsSection.submitStats(user?.stats, savedInstanceState == null)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.swipeRefreshLayout.apply {
                    isRefreshing = isRefreshing && state.isRefreshing
                }
                statsSection.setLoading(state.isInitialLoading)
            }
        }
    }

    private fun updateUser(user: User?) {
        binding.user = user
        binding.invalidateAll()

        user?.waifu?.let { waifu ->
            Glide.with(this).asBitmap()
                .load(waifu.image?.originalOrDown())
                .circleCrop()
                .dontAnimate()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.layoutWaifuRow.icon = resource.toDrawable(resources)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        user?.favorites?.let { updateFavoritesData(it) }
    }

    private fun updateProfileLinks(profileLinks: List<ProfileLink>) {
        linksSection.submitProfileLinks(profileLinks)
    }

    private fun updateFavoritesData(favorites: List<Favorite>) {
        favoritesSection.submitFavorites(favorites)
    }

    protected abstract fun onFavoriteMediaItemClicked(view: View, media: Media)

    protected abstract fun openCharacterDetailsBottomSheet(character: Character)

    protected abstract fun navigateToFollowList(type: FollowListType)

    private fun onProfileLinkClicked(profileLink: ProfileLink) {
        profileLink.url?.let { url ->
            if (URLUtil.isValidUrl(url)) {
                openUrl(url)
            } else {
                copyToClipboard(profileLink.profileLinkSite?.name ?: "URL", url)
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (view == null) return
        binding.nsvContent.smoothScrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favoritesSection.clear()
    }
}