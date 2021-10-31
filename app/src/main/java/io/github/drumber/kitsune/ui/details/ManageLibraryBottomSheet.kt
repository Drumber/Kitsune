package io.github.drumber.kitsune.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.databinding.SheetManageLibraryBinding

class ManageLibraryBottomSheet : BottomSheetDialogFragment() {

    private val args: ManageLibraryBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = SheetManageLibraryBinding.inflate(inflater, container, false)
        binding.title = args.model.title
        return binding.root
    }

}