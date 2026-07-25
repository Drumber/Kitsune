package io.github.drumber.kitsune.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.ui.compose.composeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaMappingsBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: DetailsViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val state by viewModel.mappingsSate.collectAsStateWithLifecycle()
        MediaMappingsScreen(
            state = state,
            onOpenUrl = { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        )
    }

    companion object {
        const val TAG = "media_mappings_bottom_sheet"
    }
}
