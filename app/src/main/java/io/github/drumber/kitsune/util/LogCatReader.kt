package io.github.drumber.kitsune.util

import android.os.Build
import io.github.drumber.kitsune.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

object LogCatReader {

    suspend fun readAppLogs(maxLines: Int = 5000) = withContext(Dispatchers.IO) {
        val logLevelFilter = when (BuildConfig.DEBUG) {
            true -> "*:D"
            false -> "*:I"
        }
        val process = Runtime.getRuntime().exec("logcat -d -t $maxLines $logLevelFilter")
        process.inputStream.bufferedReader().use {
            return@withContext it.readLines()
        }
    }

    suspend fun writeAppLogsToFile(
        file: File,
        writeHeader: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val logs = readAppLogs()
        file.parentFile?.mkdirs()
        file.bufferedWriter().use { writer ->
            if (writeHeader)
                writer.appendLine(generateLogFileHeader())
            logs.forEach { line ->
                writer.appendLine(line)
            }
        }
    }

    private fun generateLogFileHeader(): String {
        return StringBuilder()
            .appendLine("###########################################################")
            .appendLine("Log file generated on ${Date()}")
            .appendLine("Kitsune version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            .appendLine("Application ID: ${BuildConfig.APPLICATION_ID}")
            .appendLine("Build type: ${BuildConfig.BUILD_TYPE}")
            .appendLine("Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            .appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            .appendLine("###########################################################")
            .toString()
    }

}