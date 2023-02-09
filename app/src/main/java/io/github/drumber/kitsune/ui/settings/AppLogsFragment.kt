package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentAppLogsBinding
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppLogsFragment : Fragment(R.layout.fragment_app_logs) {

    private val binding: FragmentAppLogsBinding by viewBinding()
    private val viewModel: AppLogsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.nestedScrollView.initPaddingWindowInsetsListener(left = true, right = true, bottom = true, consume = false)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.logMessages.observe(viewLifecycleOwner) { logMessages ->
            binding.apply {
                progressBar.isVisible = false
                tvNoLogs.isVisible = logMessages.isBlank()
                tvLogMessages.isVisible = logMessages.isNotBlank()
                tvLogMessages.text = logMessages

                if (savedInstanceState == null) {
                    nestedScrollView.post {
                        nestedScrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }
    }

}