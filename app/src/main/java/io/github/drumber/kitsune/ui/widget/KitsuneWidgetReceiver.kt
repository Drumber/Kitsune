package io.github.drumber.kitsune.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.work.SyncLibraryEntriesForWidgetWorker
import kotlin.time.Duration.Companion.minutes

class KitsuneWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LibraryAppWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (shouldSyncLibrary()) {
            enqueueSyncLibraryWork(context)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun shouldSyncLibrary(): Boolean {
        val lastSyncMillis = KitsunePref.lastLibraryFetchForWidget
        return lastSyncMillis == -1L || System.currentTimeMillis() >= (lastSyncMillis + 5.minutes.inWholeMilliseconds)
    }

    private fun enqueueSyncLibraryWork(context: Context) {
        val workConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWork = OneTimeWorkRequestBuilder<SyncLibraryEntriesForWidgetWorker>()
            .setConstraints(workConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncLibraryEntriesForWidgetWorker.TAG,
            ExistingWorkPolicy.KEEP,
            syncWork
        )
    }
}