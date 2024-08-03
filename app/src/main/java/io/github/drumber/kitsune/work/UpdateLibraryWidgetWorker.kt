package io.github.drumber.kitsune.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.drumber.kitsune.ui.widget.LibraryAppWidget

class UpdateLibraryWidgetWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        LibraryAppWidget().updateAll(context)
        return Result.success()
    }

    companion object {
        const val TAG = "updateLibraryWidget"
    }
}