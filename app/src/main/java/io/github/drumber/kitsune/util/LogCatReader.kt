package io.github.drumber.kitsune.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LogCatReader {

    suspend fun readAppLogs() = withContext(Dispatchers.IO) {
        val process = Runtime.getRuntime().exec("logcat -d")
        process.inputStream.bufferedReader().use {
            return@withContext it.readLines()
        }
    }

}