package io.github.drumber.kitsune.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.ui.compose.composeView

class RatingBottomSheet : BottomSheetDialogFragment() {

    private val args: RatingBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        RatingScreen(
            title = args.title,
            ratingTwenty = args.ratingTwenty.takeIf { it != -1 },
            ratingSystem = args.ratingSystem,
            onRate = { ratingTwenty ->
                setFragmentResult(args.ratingResultKey, bundleOf(BUNDLE_RATING to ratingTwenty))
                dismiss()
            },
            onRemoveRating = {
                setFragmentResult(args.removeResultKey, bundleOf(BUNDLE_RATING to null))
                dismiss()
            },
            onDismiss = { dismiss() }
        )
    }

    companion object {
        const val BUNDLE_RATING = "rating_bundle_key"
    }
}