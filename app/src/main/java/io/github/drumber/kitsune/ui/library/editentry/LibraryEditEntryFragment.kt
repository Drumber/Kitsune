package io.github.drumber.kitsune.ui.library.editentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.base.BaseDialogFragment
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.library.RatingBottomSheet
import io.github.drumber.kitsune.ui.library.editentry.LibraryEditEntryViewModel.LoadState
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import io.github.drumber.kitsune.util.stripTimeUtcMillis
import io.github.drumber.kitsune.util.toDate
import io.github.drumber.kitsune.util.ui.DateValidatorPointBetween
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryEditEntryFragment : BaseDialogFragment(0) {

    private val args: LibraryEditEntryFragmentArgs by navArgs()

    private val viewModel: LibraryEditEntryViewModel by viewModel()

    companion object {
        const val RESULT_KEY_RATING = "library_edit_rating_result_key"
        const val RESULT_KEY_REMOVE_RATING = "library_edit_remove_rating_result_key"
    }

    override fun onStart() {
        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        LibraryEditEntryScreen(
            onDismiss = { dismiss() },
            onOpenStartedDatePicker = { openStartedDatePicker() },
            onOpenFinishedDatePicker = { openFinishedDatePicker() },
            onShowRatingSheet = { showRatingBottomSheet() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initLibraryEntry(args.libraryEntryId)

        viewModel.loadState.observe(viewLifecycleOwner) { state ->
            if (state == LoadState.CloseDialog) {
                args.entryUpdatedResultKey?.let { setFragmentResult(it, bundleOf()) }
                dismiss()
            }
        }

        setFragmentResultListener(RESULT_KEY_RATING) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateLibraryEntry { it.copy(ratingTwenty = rating) }
            }
        }

        setFragmentResultListener(RESULT_KEY_REMOVE_RATING) { _, _ ->
            val oldRating = viewModel.uneditedLibraryEntryWrapper?.ratingTwenty
            val rating = if (oldRating == null) null else -1
            viewModel.updateLibraryEntry { it.copy(ratingTwenty = rating) }
        }
    }

    private fun openStartedDatePicker() {
        val wrapper = viewModel.libraryEntryWithModification.value ?: return
        val selection = wrapper.startedAt?.parseUtcDate()?.time
            ?.stripTimeUtcMillis() ?: MaterialDatePicker.todayInUtcMilliseconds()

        val validator = wrapper.finishedAt?.parseUtcDate()?.time
            ?.stripTimeUtcMillis()?.let {
                DateValidatorPointBackward.before(it)
            } ?: DateValidatorPointBackward.now()

        openDatePicker(
            title = getString(R.string.library_edit_started),
            selection = selection,
            validator = validator
        ) { dateMillis ->
            val dateString = dateMillis.toDate().formatDate(DATE_FORMAT_ISO)
            viewModel.updateLibraryEntry { it.copy(startedAt = dateString) }
        }
    }

    private fun openFinishedDatePicker() {
        val wrapper = viewModel.libraryEntryWithModification.value ?: return
        val selection = wrapper.finishedAt?.parseUtcDate()?.time
            ?.stripTimeUtcMillis() ?: MaterialDatePicker.todayInUtcMilliseconds()

        val validator = wrapper.startedAt?.parseUtcDate()?.time
            ?.stripTimeUtcMillis()?.let {
                DateValidatorPointBetween.nowAndFrom(it)
            } ?: DateValidatorPointBackward.now()

        openDatePicker(
            title = getString(R.string.library_edit_finished),
            selection = selection,
            validator = validator
        ) { dateMillis ->
            val dateString = dateMillis.toDate().formatDate(DATE_FORMAT_ISO)
            viewModel.updateLibraryEntry { it.copy(finishedAt = dateString) }
        }
    }

    private fun openDatePicker(
        title: String,
        selection: Long,
        validator: CalendarConstraints.DateValidator,
        action: (Long) -> Unit
    ) {
        val constraints = CalendarConstraints.Builder()
            .setValidator(validator)
            .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selection)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener(action)
        datePicker.show(parentFragmentManager, "DATE_PICKER_$title")
    }

    private fun showRatingBottomSheet() {
        val libraryEntryWrapper = viewModel.libraryEntryWithModification.value ?: return
        val libraryEntry = libraryEntryWrapper.libraryEntry
        val media = libraryEntry.media ?: return

        val action = LibraryEditEntryFragmentDirections
            .actionLibraryEditEntryFragmentToRatingBottomSheet(
                title = media.title ?: "",
                ratingTwenty = libraryEntryWrapper.ratingTwenty ?: -1,
                ratingResultKey = RESULT_KEY_RATING,
                removeResultKey = RESULT_KEY_REMOVE_RATING,
                ratingSystem = RatingSystemUtil.getRatingSystem()
            )
        findNavController().navigateSafe(R.id.libraryEditEntryFragment, action)
    }
}