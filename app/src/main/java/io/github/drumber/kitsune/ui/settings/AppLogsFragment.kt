package io.github.drumber.kitsune.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentAppLogsBinding
import io.github.drumber.kitsune.util.LogCatReader
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class AppLogsFragment : Fragment(R.layout.fragment_app_logs) {

    private var _binding: FragmentAppLogsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppLogsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.nestedScrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.menu_share_app_logs) {
                shareLogFile()
            }
            true
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

    @SuppressLint("SimpleDateFormat")
    private fun shareLogFile() {
        val dateTime = SimpleDateFormat("yyy-MM-dd_HH-mm-ss").format(Date())
        val fileName = "Kitsune_$dateTime.txt"
        val logsDir = File(requireContext().cacheDir, "logs")
        val logFile = File(logsDir, fileName)

        deleteAllFiles(logsDir) // delete any previously created log files
        logFile.deleteOnExit()

        lifecycleScope.launch {
            LogCatReader.writeAppLogsToFile(logFile)

            val contentUri = FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                logFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
            }
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getText(R.string.action_share_app_logs)
                )
            )
        }
    }

    private fun deleteAllFiles(directory: File) {
        directory.listFiles { file: File -> file.isFile }?.forEach { it.delete() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}