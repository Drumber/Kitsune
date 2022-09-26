package io.github.drumber.kitsune.ui.library

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.SheetLibraryRatingBinding
import io.github.drumber.kitsune.util.RatingSystemUtil
import io.github.drumber.kitsune.util.RatingSystemUtil.convertFrom
import io.github.drumber.kitsune.util.RatingSystemUtil.convertToRatingTwenty
import io.github.drumber.kitsune.util.RatingSystemUtil.fromRatingTwentyTo
import io.github.drumber.kitsune.util.RatingSystemUtil.stepSize
import io.github.drumber.kitsune.util.RatingSystemUtil.toRatingTwentyFrom

class RatingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetLibraryRatingBinding? = null
    private val binding get() = _binding!!

    private val args: RatingBottomSheetArgs by navArgs()

    private val ratingSystem
        get() = args.ratingSystem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetLibraryRatingBinding.inflate(inflater, container, false)

        val ratingTwenty = args.ratingTwenty.takeIf { it != -1 }
        val hasNoRating = ratingTwenty == null || ratingTwenty == 0

        binding.apply {
            title = args.title

            ratingBar.apply {
                setOnRatingChangeListener { ratingBar, rating ->
                    val isInRange = ratingSystem.convertToRatingTwenty(rating) in 1..20
                    if (!isInRange) {
                        val coercedRatingTwenty = ratingSystem.convertToRatingTwenty(rating)
                            .coerceIn(1..20)
                        ratingBar.post {
                            ratingBar.rating = ratingSystem.convertFrom(coercedRatingTwenty)
                        }
                    }
                    btnRate.isEnabled = isInRange
                    updateRatingTextView()
                }
                numStars = ratingSystem.convertFrom(20).toInt()
                stepSize = ratingSystem.stepSize()
                ratingTwenty?.fromRatingTwentyTo(ratingSystem)?.let {
                    rating = it
                }
            }

            btnCancel.setOnClickListener { dismiss() }
            btnRate.setOnClickListener { onRateClicked() }
            btnRate.isEnabled = !hasNoRating
            btnRate.setText(if (hasNoRating) R.string.action_rate else R.string.action_update_rating)
            btnRemoveRating.setOnClickListener { onRemoveRatingClicked() }
            btnRemoveRating.isVisible = !hasNoRating
        }

        updateRatingTextView()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateRatingTextView() {
        val rating = binding.ratingBar.rating
        binding.tvRating.text = if (rating == 0.0f) {
            getString(R.string.library_not_rated)
        } else {
            "$rating / ${RatingSystemUtil.formatRating(20, ratingSystem)}"
        }
    }

    private fun onRateClicked() {
        val ratingTwenty = binding.ratingBar.rating.toRatingTwentyFrom(ratingSystem)
        setFragmentResult(args.ratingResultKey, bundleOf(BUNDLE_RATING to ratingTwenty))
        dismiss()
    }

    private fun onRemoveRatingClicked() {
        setFragmentResult(args.removeResultKey, bundleOf(BUNDLE_RATING to null))
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val BUNDLE_RATING = "rating_bundle_key"
    }

}