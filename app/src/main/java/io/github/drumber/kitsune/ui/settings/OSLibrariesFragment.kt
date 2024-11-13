package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import com.mikepenz.aboutlibraries.LibsBuilder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentOsLibrariesBinding
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener

class OSLibrariesFragment : Fragment(R.layout.fragment_os_libraries) {

    private var _binding: FragmentOsLibrariesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOsLibrariesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}