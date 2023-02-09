package io.github.drumber.kitsune.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LogCatReader {

    suspend fun readAppLogs(maxLines: Int = 5000) = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec("logcat -d -t $maxLines")
        process.inputStream.bufferedReader().use {
            return@withContext it.readLines()
        }
    }

    suspend fun writeAppLogsToFile(file: File) = withContext(Dispatchers.IO) {
        val logs = readAppLogs()
        file.parentFile?.mkdirs()
        file.bufferedWriter().use { writer ->
            logs.forEach { line ->
                writer.appendLine(line)
            }
        }
    }

}