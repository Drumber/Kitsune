package io.github.drumber.kitsune.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.report.ReportReason
import io.github.drumber.kitsune.databinding.SheetReportPostBinding
import io.github.drumber.kitsune.util.extensions.afterTextChanged
import io.github.drumber.kitsune.util.extensions.text
import io.github.drumber.kitsune.util.ui.showSnackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReportBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetReportPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModel {
        parametersOf(requireArguments().getString(BUNDLE_POST_ID))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetReportPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSubmit.setOnClickListener { viewModel.submitReport() }

        val reasonAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            ReportReason.entries.map { getString(it.getStringRes()) }
        )
        (binding.menuReason.editText as AutoCompleteTextView).apply {
            setAdapter(reasonAdapter)
            setOnItemClickListener { _, _, position, _ ->
                viewModel.selectReason(ReportReason.entries[position])
            }
        }

        binding.fieldExplanation.afterTextChanged { viewModel.setExplanation(it) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    val state = uiState.state
                    binding.isLoading = state is ReportViewModel.ReportState.Loading
                    binding.alreadyReported = state is ReportViewModel.ReportState.AlreadyReported

                    val reasonText = uiState.selectedReason?.let { getString(it.getStringRes()) }
                    (binding.menuReason.editText as AutoCompleteTextView).setText(reasonText, false)

                    val isExplanationRequired = uiState.selectedReason == ReportReason.OTHER
                            && uiState.explanation.isNullOrBlank()
                    binding.fieldExplanation.error = if (isExplanationRequired) {
                        getString(R.string.report_error_explanation_required)
                    } else {
                        null
                    }
                    if (binding.fieldExplanation.text().isBlank()) {
                        binding.fieldExplanation.editText?.setText(uiState.explanation)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canSubmit.collectLatest { canSubmit ->
                    binding.btnSubmit.isEnabled = canSubmit
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitEvent.collect { event ->
                    when (event) {
                        is ReportViewModel.SubmitEvent.ReportSent -> {
                            val parent = parentFragment?.view ?: binding.root
                            showSnackbar(parent, R.string.report_success)
                            dismiss()
                        }

                        is ReportViewModel.SubmitEvent.Error -> {
                            showSnackbar(binding.root, R.string.report_failure)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ReportReason.getStringRes(): Int = when (this) {
        ReportReason.NSFW -> R.string.report_reason_nsfw
        ReportReason.OFFENSIVE -> R.string.report_reason_offensive
        ReportReason.SPOILER -> R.string.report_reason_spoiler
        ReportReason.BULLYING -> R.string.report_reason_bullying
        ReportReason.OTHER -> R.string.report_reason_other
        ReportReason.SPAM -> R.string.report_reason_spam
    }

    companion object {
        const val TAG = "report_bottom_sheet"
        const val BUNDLE_POST_ID = "post_id_bundle_key"
    }
}
