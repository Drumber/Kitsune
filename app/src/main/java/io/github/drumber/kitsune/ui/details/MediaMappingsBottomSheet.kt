package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.databinding.SheetMediaMappingsBinding
import io.github.drumber.kitsune.ui.adapter.MediaMappingsAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaMappingsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetMediaMappingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailsViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetMediaMappingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MediaMappingsAdapter(requireContext(), mutableListOf())
        binding.listMediaMappings.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mappingsSate.collectLatest { state ->
                binding.progressBarMediaMappings.isVisible = state is MediaMappingsSate.Loading
                binding.tvErrorMediaMappings.isVisible = state is MediaMappingsSate.Error
                binding.listMediaMappings.isVisible = state is MediaMappingsSate.Success

                if (state is MediaMappingsSate.Success) {
                    adapter.dataSource.clear()
                    adapter.dataSource.addAll(state.mappings.sortedBy { it.externalSite })
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "media_mappings_bottom_sheet"
    }

}