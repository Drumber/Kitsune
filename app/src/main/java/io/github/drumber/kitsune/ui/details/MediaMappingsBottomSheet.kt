package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.data.presentation.model.mapping.getExternalUrl
import io.github.drumber.kitsune.databinding.SheetMediaMappingsBinding
import io.github.drumber.kitsune.ui.adapter.MediaMappingsAdapter
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaMappingsBottomSheet : BottomSheetDialogFragment() {

    private val binding by viewBinding(SheetMediaMappingsBinding::bind)
    private val viewModel: DetailsViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return SheetMediaMappingsBinding.inflate(inflater, container, false).root
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
                    val mappings = state.mappings
                        .distinctBy { it.getExternalUrl() ?: it.externalSite }
                        .sortedBy { it.externalSite }
                    adapter.dataSource.clear()
                    adapter.dataSource.addAll(mappings)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val TAG = "media_mappings_bottom_sheet"
    }

}