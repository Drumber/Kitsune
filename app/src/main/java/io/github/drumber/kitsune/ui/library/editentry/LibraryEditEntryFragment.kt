package io.github.drumber.kitsune.ui.library.editentry

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.htmlEncode
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.databinding.FragmentEditLibraryEntryBinding
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.Manga
import io.github.drumber.kitsune.domain.model.ui.library.getStringResId
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.ui.base.BaseDialogFragment
import io.github.drumber.kitsune.ui.library.RatingBottomSheet
import io.github.drumber.kitsune.ui.library.editentry.LibraryEditEntryViewModel.LoadState
import io.github.drumber.kitsune.ui.widget.CustomNumberSpinner
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import io.github.drumber.kitsune.util.RatingSystemUtil
import io.github.drumber.kitsune.util.RatingSystemUtil.formatRatingTwenty
import io.github.drumber.kitsune.util.extensions.getResourceId
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.formatUtcDate
import io.github.drumber.kitsune.util.getLocalCalendar
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.stripTimeUtcMillis
import io.github.drumber.kitsune.util.toDate
import io.github.drumber.kitsune.util.ui.DateValidatorPointBetween
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryEditEntryFragment : BaseDialogFragment(R.layout.fragment_edit_library_entry) {

    private val args: LibraryEditEntryFragmentArgs by navArgs()

    private var _binding: FragmentEditLibraryEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryEditEntryViewModel by viewModel()

    private var listenersInitialized = false

    companion object {
        const val RESULT_KEY_RATING = "library_edit_rating_result_key"
        const val RESULT_KEY_REMOVE_RATING = "library_edit_remove_rating_result_key"
    }

    private val libraryStatusMenuItems = listOf(
        LibraryStatus.Current,
        LibraryStatus.Planned,
        LibraryStatus.Completed,
        LibraryStatus.OnHold,
        LibraryStatus.Dropped
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditLibraryEntryBinding.inflate(inflater, container, false)
        viewModel.initLibraryEntry(args.libraryEntryId)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.nestedScrollView.initMarginWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )
        binding.layoutBottomBar.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                dismiss()
            }

            TooltipCompat.setTooltipText(
                btnRemoveEntry,
                getString(R.string.library_action_remove)
            )

            btnSaveChanges.setOnClickListener {
                viewModel.saveChanges()
            }
            btnRemoveEntry.setOnClickListener {
                val libraryEntry = viewModel.libraryEntry.value
                val mediaAdapter = (libraryEntry?.anime ?: libraryEntry?.manga)
                    ?.let { MediaAdapter.fromMedia(it) }

                val dialogMsg = getString(
                    R.string.dialog_remove_from_library_msg,
                    mediaAdapter?.title?.htmlEncode() ?: getString(R.string.no_information)
                ).parseAsHtml()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_remove_from_library_title)
                    .setMessage(dialogMsg)
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.action_remove) { dialog, _ ->
                        dialog.dismiss()
                        viewModel.removeLibraryEntry()
                    }
                    .show()
            }
        }

        viewModel.loadState.observe(viewLifecycleOwner) { state ->
            if (state == LoadState.CloseDialog) {
                args.entryUpdatedResultKey?.let {
                    setFragmentResult(it, bundleOf())
                }
                dismiss()
            } else {
                binding.layoutLoading.isVisible = state == LoadState.Loading
                if (state == LoadState.Error) {
                    Snackbar.make(
                        binding.root,
                        R.string.error_library_update_failed,
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(binding.cardBottomBar)
                        .show()
                }
            }
        }

        viewModel.libraryEntry.observe(viewLifecycleOwner) { libraryEntry ->
            val mediaAdapter = (libraryEntry.anime ?: libraryEntry.manga)
                ?.let { MediaAdapter.fromMedia(it) }

            binding.tvTitle.text = mediaAdapter?.title

            binding.tvMediaInfo.text = mediaAdapter?.let { "${it.publishingYear} • ${it.subtype}" }

            Glide.with(this)
                .load(mediaAdapter?.posterImage)
                .addTransform(RoundedCorners(8))
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }

        viewModel.libraryEntryWrapper.observe(viewLifecycleOwner) { wrapper ->
            val mediaAdapter = (wrapper.libraryEntry.anime ?: wrapper.libraryEntry.manga)
                ?.let { MediaAdapter.fromMedia(it) }

            binding.apply {
                setLibraryStatusMenu(mediaAdapter, wrapper.status)

                mediaAdapter?.episodeOrChapterCount?.let {
                    spinnerProgress.setMaxValue(it)
                } ?: spinnerProgress.setSuffixMode(CustomNumberSpinner.SuffixMode.Disabled)
                spinnerProgress.setValue(wrapper.progress ?: 0)

                layoutVolumes.isVisible = mediaAdapter?.isAnime() == false
                spinnerVolumes.setMaxValue((mediaAdapter?.media as? Manga)?.volumeCount ?: 0)
                spinnerVolumes.setValue(wrapper.volumesOwned ?: 0)

                val ratingTwenty = wrapper.ratingTwenty
                val hasRated = ratingTwenty != null && ratingTwenty != -1
                fieldRating.editText?.apply {
                    val ratingText = if (hasRated) {
                        "${ratingTwenty!!.formatRatingTwenty()} / ${20.formatRatingTwenty()}"
                    } else {
                        getString(R.string.library_not_rated)
                    }
                    setText(ratingText)
                }
                fieldRating.setEndIconDrawable(
                    if (hasRated)
                        R.drawable.ic_star_24
                    else
                        R.drawable.ic_star_outline_24
                )
                fieldRating.setEndIconTintList(ColorStateList.valueOf(getControlColor(hasRated)))

                tvReconsumeLabel.text = getString(
                    if (mediaAdapter?.isAnime() != false)
                        R.string.library_edit_rewatch_count
                    else
                        R.string.library_edit_reread_count
                )
                spinnerReconsume.setValue(wrapper.reconsumeCount ?: 0)
                spinnerReconsume.setActionTooltip(
                    getString(
                        if (mediaAdapter?.isAnime() != false)
                            R.string.library_edit_start_rewatch
                        else
                            R.string.library_edit_start_reread
                    )
                )

                val privacyAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_dropdown,
                    listOf(
                        getString(R.string.library_edit_privacy_public),
                        getString(R.string.library_edit_privacy_private)
                    )
                )
                (menuPrivacy.editText as? AutoCompleteTextView)?.apply {
                    setAdapter(privacyAdapter)
                    val currValue = if (wrapper.isPrivate == true)
                        getString(R.string.library_edit_privacy_private)
                    else
                        getString(R.string.library_edit_privacy_public)
                    setText(currValue, false)
                }

                val startedText = wrapper.startedAt?.parseUtcDate()?.formatDate()
                    ?: getString(R.string.library_edit_no_date_set)
                fieldStarted.editText?.setText(startedText)

                val finishedText = wrapper.finishedAt?.parseUtcDate()?.formatDate()
                    ?: getString(R.string.library_edit_no_date_set)
                fieldFinished.editText?.setText(finishedText)

                fieldNotes.editText?.apply {
                    if (text.toString() != (wrapper.notes ?: "")) {
                        setText(wrapper.notes)
                    }
                }
            }

            if (!listenersInitialized) {
                initListeners()
            }
        }

        viewModel.hasChanges.observe(viewLifecycleOwner) { hasChanges ->
            binding.btnSaveChanges.isEnabled = hasChanges
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

    private fun initListeners() {
        listenersInitialized = true
        binding.apply {
            (menuLibraryStatus.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
                val status = libraryStatusMenuItems[position]
                viewModel.updateLibraryEntry { it.copy(status = status) }
            }

            spinnerProgress.setValueChangedListener { value ->
                val wrapper = viewModel.libraryEntryWrapper.value
                val mediaAdapter = (wrapper?.libraryEntry?.anime ?: wrapper?.libraryEntry?.manga)
                    ?.let { MediaAdapter.fromMedia(it) }

                if (value == mediaAdapter?.episodeOrChapterCount) {
                    viewModel.updateLibraryEntry {
                        it.copy(
                            progress = value,
                            status = LibraryStatus.Completed,
                            finishedAt = wrapper?.finishedAt ?: getLocalCalendar().formatUtcDate()
                        )
                    }
                } else {
                    viewModel.updateLibraryEntry { it.copy(progress = value) }
                }
            }

            spinnerVolumes.setValueChangedListener { value ->
                viewModel.updateLibraryEntry { it.copy(volumesOwned = value) }
            }

            fieldRating.editText?.setOnClickListener {
                showRatingBottomSheet()
            }

            spinnerReconsume.setValueChangedListener { value ->
                viewModel.updateLibraryEntry { it.copy(reconsumeCount = value) }
            }

            // start reconsume action
            spinnerReconsume.setActionClickListener {
                val wrapper = viewModel.libraryEntryWrapper.value
                viewModel.updateLibraryEntry {
                    it.copy(
                        progress = 0,
                        reconsumeCount = wrapper?.reconsumeCount?.plus(1) ?: 1,
                        status = LibraryStatus.Current
                    )
                }
            }

            (menuPrivacy.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
                val isPrivate = position == 1
                viewModel.updateLibraryEntry { it.copy(privateEntry = isPrivate) }
            }

            fieldStarted.editText?.setOnClickListener {
                val wrapper = viewModel.libraryEntryWrapper.value
                val selection = wrapper?.startedAt?.parseUtcDate()?.time
                    ?.stripTimeUtcMillis() ?: MaterialDatePicker.todayInUtcMilliseconds()

                val validator = wrapper?.finishedAt?.parseUtcDate()?.time
                    ?.stripTimeUtcMillis()?.let {
                        DateValidatorPointBackward.before(it)
                    } ?: DateValidatorPointBackward.now()

                openDatePicker(
                    getString(R.string.library_edit_started),
                    selection,
                    validator
                ) { dateMillis ->
                    val dateString = dateMillis.toDate().formatDate(DATE_FORMAT_ISO)
                    viewModel.updateLibraryEntry { it.copy(startedAt = dateString) }
                }
            }

            btnResetStarted.setOnClickListener {
                val oldStartedAt = viewModel.uneditedLibraryEntryWrapper?.startedAt
                viewModel.updateLibraryEntry { it.copy(startedAt = oldStartedAt) }
            }

            fieldFinished.editText?.setOnClickListener {
                val wrapper = viewModel.libraryEntryWrapper.value
                val selection = wrapper?.finishedAt?.parseUtcDate()?.time
                    ?.stripTimeUtcMillis() ?: MaterialDatePicker.todayInUtcMilliseconds()

                val validator = wrapper?.startedAt?.parseUtcDate()?.time
                    ?.stripTimeUtcMillis()?.let {
                        DateValidatorPointBetween.nowAndFrom(it)
                    } ?: DateValidatorPointBackward.now()

                openDatePicker(
                    getString(R.string.library_edit_finished),
                    selection,
                    validator
                ) { dateMillis ->
                    val dateString = dateMillis.toDate().formatDate(DATE_FORMAT_ISO)
                    viewModel.updateLibraryEntry { it.copy(finishedAt = dateString) }
                }
            }

            btnResetFinished.setOnClickListener {
                val oldFinishedAt = viewModel.uneditedLibraryEntryWrapper?.finishedAt
                viewModel.updateLibraryEntry { it.copy(finishedAt = oldFinishedAt) }
            }

            fieldNotes.editText?.doAfterTextChanged { text ->
                val oldNotes = viewModel.uneditedLibraryEntryWrapper?.notes
                val note = if (oldNotes == null && text?.toString().isNullOrBlank()) {
                    // if old note is null and we haven't edited the note, then set it to null
                    null
                } else {
                    text?.toString()
                }
                viewModel.updateLibraryEntry { it.copy(notes = note) }
            }
        }
    }

    private fun setLibraryStatusMenu(mediaAdapter: MediaAdapter?, currValue: LibraryStatus?) {
        val isAnime = mediaAdapter?.isAnime() != false
        val statusItems = libraryStatusMenuItems.map { getString(it.getStringResId(isAnime)) }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            statusItems
        )

        (binding.menuLibraryStatus.editText as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            currValue?.let { setText(getString(it.getStringResId(isAnime)), false) }
        }
    }

    private fun showRatingBottomSheet() {
        val libraryEntryWrapper = viewModel.libraryEntryWrapper.value ?: return
        val libraryEntry = viewModel.libraryEntryWrapper.value?.libraryEntry ?: return
        val media = libraryEntry.anime ?: libraryEntry.manga ?: return
        val mediaAdapter = MediaAdapter.fromMedia(media)

        val action =
            LibraryEditEntryFragmentDirections.actionLibraryEditEntryFragmentToRatingBottomSheet(
                title = mediaAdapter.title ?: "",
                ratingTwenty = libraryEntryWrapper.ratingTwenty ?: -1,
                ratingResultKey = RESULT_KEY_RATING,
                removeResultKey = RESULT_KEY_REMOVE_RATING,
                ratingSystem = RatingSystemUtil.getRatingSystem()
            )
        findNavController().navigateSafe(R.id.libraryEditEntryFragment, action)
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

    private fun getControlColor(accent: Boolean): Int {
        return ContextCompat.getColor(
            requireContext(), requireActivity().theme.getResourceId(
                if (accent) R.attr.colorPrimary else R.attr.colorControlNormal
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}