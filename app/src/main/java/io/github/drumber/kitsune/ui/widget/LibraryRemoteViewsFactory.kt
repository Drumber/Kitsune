package io.github.drumber.kitsune.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.LibraryWidget
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList

class LibraryRemoteViewsFactory(
    private val context: Context,
    private val libraryRepository: LibraryRepository,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private val libraryEntries = CopyOnWriteArrayList<LibraryEntry>()

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        updateData()
    }

    override fun onDestroy() {
        libraryEntries.clear()
    }

    override fun getCount(): Int {
        return libraryEntries.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val data = ArrayList(libraryEntries)
        if (position >= data.size) {
            return RemoteViews(context.packageName, R.layout.widget_library_item).apply {
                setTextViewText(R.id.widget_library_title, "No data")
            }
        }

        val entry = data[position]
        return RemoteViews(context.packageName, R.layout.widget_library_item).apply {
            setTextViewText(R.id.widget_library_title, entry.media?.title)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        val data = ArrayList(libraryEntries)
        if (position >= data.size) {
            return -1
        }
        return data[position].id.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun updateData() {
        runBlocking {
            try {
                val filter = LibraryEntryFilter(
                    kind = LibraryEntryKind.All,
                    libraryStatus = listOf(LibraryStatus.Current)
                )
                val entries = libraryRepository.getLibraryEntriesByFilterFromDatabase(filter)
                    .take(LibraryWidget.MAX_ITEM_COUNT)
                libraryEntries.clear()
                libraryEntries.addAll(entries)
            } catch (e: Exception) {
                logE("Failed to fetch library entries for widget $appWidgetId", e)
            }
        }
    }
}