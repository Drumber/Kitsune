package io.github.drumber.kitsune.ui.library

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.SheetLibraryRatingBinding

class RatingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetLibraryRatingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetLibraryRatingBinding.inflate(inflater, container, false)

        val ratingTwenty = arguments?.getInt(BUNDLE_RATING)
        val hasNoRating = ratingTwenty == null || ratingTwenty == 0

        binding.apply {
            title = arguments?.getString(BUNDLE_TITLE)

            ratingBar.apply {
                setOnRatingChangeListener { ratingBar, rating ->
                    val isInRange = rating in 0.5f..5.0f
                    if (!isInRange) {
                        ratingBar.post {
                            ratingBar.rating = rating.coerceIn(0.5f..5.0f)
                        }
                    }
                    btnRate.isEnabled = isInRange
                    updateRatingTextView()
                }
                stepSize = 0.5f
                ratingTwenty?.div(4.0f)?.let {
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
            "$rating / 5.0"
        }
    }

    private fun onRateClicked() {
        val rating = binding.ratingBar.rating.times(4.0f).toInt()
        setFragmentResult(RATING_REQUEST_KEY, bundleOf(BUNDLE_RATING to rating))
        dismiss()
    }

    private fun onRemoveRatingClicked() {
        setFragmentResult(REMOVE_RATING_REQUEST_KEY, bundleOf(BUNDLE_RATING to null))
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "library_rating_bottom_sheet"
        const val BUNDLE_TITLE = "title_bundle_key"
        const val BUNDLE_RATING = "rating_bundle_key"
        const val RATING_REQUEST_KEY = "rating_request_key"
        const val REMOVE_RATING_REQUEST_KEY = "remove_rating_request_key"
    }

}