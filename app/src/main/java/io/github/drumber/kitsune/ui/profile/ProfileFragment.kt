package io.github.drumber.kitsune.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.ProfilePictureBehavior
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment(R.layout.fragment_profile, true) {

    private val binding: FragmentProfileBinding by viewBinding()

    private val viewModel: ProfileViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userModel.observe(viewLifecycleOwner) { user ->
            binding.user = user

            val glide = GlideApp.with(this)

            glide.load(user?.avatar)
                .dontAnimate()
                .placeholder(R.drawable.profile_picture_placeholder)
                .into(binding.ivProfileImage)

            glide.load(user?.coverImage)
                .centerCrop()
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)
        }

        binding.apply {
            btnSettings.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
                findNavController().navigate(action)
            }
            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }

            ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbar) { _, insets -> insets }
            coverSpacer.initMarginWindowInsetsListener(left = true, top = true, right = true)
            toolbar.initWindowInsetsListener(consume = false)

            ViewCompat.setOnApplyWindowInsetsListener(ivProfileImage) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val params = ivProfileImage.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = params.behavior as ProfilePictureBehavior
                behavior.offsetX = insets.left.toFloat()
                behavior.offsetY = insets.top.toFloat()
                windowInsets
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateUserModel()
    }

}