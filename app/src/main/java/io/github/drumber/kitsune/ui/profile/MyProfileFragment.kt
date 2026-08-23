package io.github.drumber.kitsune.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyProfileFragment : BaseProfileFragment() {

    override val viewModel: MyProfileViewModel by viewModel()

    override val useSocialImageLoader = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        updateOptionsMenu()

        binding.btnLogin.setOnClickListener {
            val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
            startActivity(intent)
        }

        binding.fabPostWall.setOnClickListener {
            val action = MyProfileFragmentDirections.actionGlobalCreatePostFragment()
            val extras = FragmentNavigatorExtras(it to getString(R.string.create_post_transition_name))
            findNavController().navigateSafe(R.id.profile_fragment, action, extras)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val savedState = findNavController().currentBackStackEntry?.savedStateHandle
                savedState?.getStateFlow("refreshFavorites", false)
                    ?.collectLatest { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.refreshUser()
                            savedState["refreshFavorites"] = false
                        }
                    }
            }
        }
    }

    override fun onUserModelChanged(user: User?) {
        super.onUserModelChanged(user)
        binding.nsvNotLoggedIn.isVisible = user == null
        updateOptionsMenu()
    }

    private fun initToolbar() {
        binding.apply {
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> {
                        val action = MyProfileFragmentDirections
                            .actionProfileFragmentToSettingsNavGraph()
                        findNavController().navigate(action)
                    }

                    R.id.menu_edit_profile -> {
                        val action = MyProfileFragmentDirections
                            .actionProfileFragmentToEditProfileFragment()
                        findNavController().navigateSafe(R.id.profile_fragment, action)
                    }

                    R.id.menu_share_profile_url -> {
                        val user = viewModel.getUser()
                        val profileId = user?.slug ?: user?.id
                        if (profileId != null) {
                            val url = Kitsu.USER_URL_PREFIX + profileId
                            startUrlShareIntent(url)
                        } else {
                            showSomethingWrongToast()
                        }
                    }

                    R.id.menu_log_out -> {
                        showLogOutConfirmationDialog()
                    }
                }
                true
            }
        }
    }

    override fun createProfileViewPagerAdapter(userId: String): ProfileViewPagerAdapter {
        return ProfileViewPagerAdapter(userId, true, R.id.profile_fragment, this)
    }

    private fun updateOptionsMenu() {
        val isLoggedIn = viewModel.getUser() != null
        binding.toolbar.menu.apply {
            findItem(R.id.menu_edit_profile).isVisible = isLoggedIn
            findItem(R.id.menu_log_out).isVisible = isLoggedIn
            findItem(R.id.menu_share_profile_url).isVisible = isLoggedIn
        }
    }

    private fun showLogOutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.action_log_out)
            .setMessage(R.string.dialog_log_out_confirmation)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.action_log_out) { dialog, _ ->
                onLogOut()
                dialog.dismiss()
            }
            .show()
    }

    private fun onLogOut() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logOut()
        }
    }
}
