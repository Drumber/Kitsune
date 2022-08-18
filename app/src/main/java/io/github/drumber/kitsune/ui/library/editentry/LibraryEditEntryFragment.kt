package io.github.drumber.kitsune.ui.library.editentry

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.os.bundleOf
import androidx.core.text.htmlEncode
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.EdgeToEdgeUtils
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.addTransform
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.model.library.getStringResId
import io.github.drumber.kitsune.data.model.media.Manga
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.FragmentEditLibraryEntryBinding
import io.github.drumber.kitsune.ui.library.RatingBottomSheet
import io.github.drumber.kitsune.util.*
import io.github.drumber.kitsune.util.extensions.setMaxLinesFitHeight
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryEditEntryFragment : DialogFragment() {

    private val args: LibraryEditEntryFragmentArgs by navArgs()

    private var _binding: FragmentEditLibraryEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryEditEntryViewModel by viewModel()

    private var listenersInitialized = false

    private val libraryStatusMenuItems = listOf(
        Status.Current,
        Status.Planned,
        Status.Completed,
        Status.OnHold,
        Status.Dropped
    )

    override fun getTheme(): Int {
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.fullScreenDialogTheme, typedValue, true)
        return typedValue.data
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes?.dimAmount = 0.8f
            setWindowAnimations(R.style.Theme_Kitsune_Slide)
            EdgeToEdgeUtils.applyEdgeToEdge(this, true)
        }
    }

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

            tvTitle.setMaxLinesFitHeight()
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

        viewModel.libraryEntry.observe(viewLifecycleOwner) { libraryEntry ->
            val mediaAdapter = (libraryEntry.anime ?: libraryEntry.manga)
                ?.let { MediaAdapter.fromMedia(it) }

            binding.tvTitle.text = mediaAdapter?.title

            GlideApp.with(this)
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

                spinnerProgress.setMaxValue(mediaAdapter?.episodeOrChapterCount ?: 0)
                spinnerProgress.setValue(wrapper.progress ?: 0)

                layoutVolumes.isVisible = mediaAdapter?.isAnime() == false
                spinnerVolumes.setMaxValue((mediaAdapter?.media as? Manga)?.volumeCount ?: 0)
                spinnerVolumes.setValue(wrapper.volumesOwned ?: 0)

                val ratingTwenty = wrapper.ratingTwenty
                val hasRated = ratingTwenty != null && ratingTwenty != -1
                fieldRating.editText?.apply {
                    val ratingText = if (hasRated) {
                        "${ratingTwenty!! / 4.0f} / 5.0"
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

                val startedText = wrapper.startedAt?.toDate(DATE_FORMAT_ISO)?.formatDate()
                    ?: getString(R.string.library_edit_no_date_set)
                fieldStarted.editText?.setText(startedText)

                val finishedText = wrapper.finishedAt?.toDate(DATE_FORMAT_ISO)?.formatDate()
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

        setFragmentResultListener(RatingBottomSheet.RATING_REQUEST_KEY) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateLibraryEntry { it.copy(ratingTwenty = rating) }
            }
        }

        setFragmentResultListener(RatingBottomSheet.REMOVE_RATING_REQUEST_KEY) { _, _ ->
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
                            status = Status.Completed,
                            finishedAt = wrapper?.finishedAt ?: todayUtcMillis().toDate()
                                .formatDate(DATE_FORMAT_ISO)
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
                        status = Status.Current
                    )
                }
            }

            (menuPrivacy.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
                val isPrivate = position == 1
                viewModel.updateLibraryEntry { it.copy(isPrivate = isPrivate) }
            }

            fieldStarted.editText?.setOnClickListener {
                val wrapper = viewModel.libraryEntryWrapper.value
                val selection = wrapper?.startedAt?.toDate(DATE_FORMAT_ISO)?.timeInMillis
                    ?.stripTimeUtcMillis() ?: MaterialDatePicker.todayInUtcMilliseconds()

                val validator = wrapper?.finishedAt?.toDate(DATE_FORMAT_ISO)?.timeInMillis
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
                val selection = wrapper?.finishedAt?.toDate(DATE_FORMAT_ISO)?.timeInMillis
                    ?.stripTimeUtcMillis() ?: MaterialDatePicker.todayInUtcMilliseconds()

                val validator = wrapper?.startedAt?.toDate(DATE_FORMAT_ISO)?.timeInMillis
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

    private fun setLibraryStatusMenu(mediaAdapter: MediaAdapter?, currValue: Status?) {
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

        val sheetLibraryRating = RatingBottomSheet()
        val bundle = bundleOf(
            RatingBottomSheet.BUNDLE_TITLE to mediaAdapter.title,
            RatingBottomSheet.BUNDLE_RATING to libraryEntryWrapper.ratingTwenty
        )
        sheetLibraryRating.arguments = bundle
        sheetLibraryRating.show(parentFragmentManager, RatingBottomSheet.TAG)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}