package io.github.drumber.kitsune.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.drumber.kitsune.constants.LibraryWidget
import io.github.drumber.kitsune.domain.library.FetchLibraryEntriesForWidgetUseCase
import io.github.drumber.kitsune.domain.library.SynchronizeLocalLibraryModificationsUseCase
import io.github.drumber.kitsune.preference.KitsunePref
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncLibraryEntriesForWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val synchronizeLocalLibraryModifications: SynchronizeLocalLibraryModificationsUseCase by inject()
    private val fetchLibraryEntriesForWidget: FetchLibraryEntriesForWidgetUseCase by inject()

    override suspend fun doWork(): Result {
        synchronizeLocalLibraryModifications()
        fetchLibraryEntriesForWidget(LibraryWidget.MAX_ITEM_COUNT)
        KitsunePref.lastLibraryFetchForWidget = System.currentTimeMillis()
        return Result.success()
    }

    companion object {
        const val TAG = "syncLibraryEntriesForWidget"
    }
}