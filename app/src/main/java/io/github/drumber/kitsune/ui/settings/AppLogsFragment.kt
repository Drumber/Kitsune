package io.github.drumber.kitsune.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.LogCatReader
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class AppLogsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        AppLogsContent()
    }

    @Composable
    private fun AppLogsContent() {
        val viewModel: AppLogsViewModel = koinViewModel()
        val logs by viewModel.logMessages.collectAsStateWithLifecycle()

        AppLogsScreen(
            logs = logs,
            onNavigateUp = { findNavController().navigateUp() },
            onShareClick = { shareLogFile() }
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun shareLogFile() {
        val dateTime = SimpleDateFormat("yyy-MM-dd_HH-mm-ss").format(Date())
        val fileName = "Kitsune_$dateTime.txt"
        val logsDir = File(requireContext().cacheDir, "logs")
        val logFile = File(logsDir, fileName)

        deleteAllFiles(logsDir)
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
}