package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mikepenz.aboutlibraries.LibsBuilder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentOsLibrariesBinding
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener

class OSLibrariesFragment : Fragment(R.layout.fragment_os_libraries) {

    private val binding: FragmentOsLibrariesBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.collapsingToolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.apply {
            initWindowInsetsListener(consume = false)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        val aboutLibrariesFragment = LibsBuilder()
            .withLicenseShown(true)
            .withEdgeToEdge(true)
            .withShowLoadingProgress(true)
            .supportFragment()

        childFragmentManager.beginTransaction()
            .replace(R.id.os_libraries_fragment_container, aboutLibrariesFragment)
            .commit()
    }

}