package io.github.drumber.kitsune.data.repository

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.work.UpdateLibraryWidgetWorker

interface LibraryChangeListener {
    fun onNewLibraryEntry(libraryEntry: LibraryEntry)
    fun onUpdateLibraryEntry(
        libraryEntryModification: LibraryEntryModification,
        updatedLibraryEntry: LibraryEntry?
    )

    fun onRemoveLibraryEntry(id: String)
    fun onDataInsertion(libraryEntries: List<LibraryEntry>)
}

class WidgetLibraryChangeListener(private val context: Context) : LibraryChangeListener {
    override fun onNewLibraryEntry(libraryEntry: LibraryEntry) {
        if (libraryEntry.status == LibraryStatus.Current)
            updateWidgets()
    }

    override fun onUpdateLibraryEntry(
        libraryEntryModification: LibraryEntryModification,
        updatedLibraryEntry: LibraryEntry?
    ) {
        updateWidgets()
    }

    override fun onRemoveLibraryEntry(id: String) {
        updateWidgets()
    }

    override fun onDataInsertion(libraryEntries: List<LibraryEntry>) {
        if (libraryEntries.any { it.status == LibraryStatus.Current })
            updateWidgets()
    }

    private fun updateWidgets() {
        val updateWidgetWork = OneTimeWorkRequestBuilder<UpdateLibraryWidgetWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UpdateLibraryWidgetWorker.TAG,
            ExistingWorkPolicy.REPLACE,
            updateWidgetWork
        )
    }
}