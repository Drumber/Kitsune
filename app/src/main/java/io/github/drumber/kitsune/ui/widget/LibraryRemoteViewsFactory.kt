package io.github.drumber.kitsune.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.LibraryWidget
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.media.identifier
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

    private val libraryEntries = CopyOnWriteArrayList<LibraryEntryWithModification>()

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
                setTextViewText(
                    R.id.widget_library_title,
                    context.getString(R.string.no_information)
                )
            }
        }

        val libraryEntry = data[position]
        return RemoteViews(context.packageName, R.layout.widget_library_item).apply {
            setTextViewText(R.id.widget_library_title, libraryEntry.media?.title)
            setTextViewText(R.id.widget_subtype, libraryEntry.media?.subtypeFormatted)
            val progressText = "${libraryEntry.progress ?: 0}/${libraryEntry.episodeCountFormatted}"
            setTextViewText(R.id.widget_progress, progressText)

            if (libraryEntry.hasEpisodesCount) {
                setProgressBar(
                    R.id.widget_progress_bar,
                    libraryEntry.media?.episodeOrChapterCount ?: 0,
                    libraryEntry.progress ?: 0,
                    false
                )
            } else {
                setViewVisibility(R.id.widget_progress_bar, View.GONE)
            }

            val fillInIntent = Intent().apply {
                val bundle = bundleOf(
                    KitsuneWidgetProvider.EXTRA_MEDIA_ID to libraryEntry.media?.id,
                    KitsuneWidgetProvider.EXTRA_MEDIA_TYPE to libraryEntry.media?.mediaType?.identifier
                )
                putExtras(bundle)
            }
            setOnClickFillInIntent(R.id.widget_btn_progress, fillInIntent)

            try {
                val bitmap = Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(libraryEntry.media?.posterImageUrl)
                    .submit()
                    .get()
                setImageViewBitmap(R.id.widget_iv_poster, bitmap)
            } catch (e: Exception) {
                logE("Failed to load poster image for widget $appWidgetId", e)
            }
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
                val entries = libraryRepository.getLibraryEntriesWithModificationsByStatus(
                    listOf(LibraryStatus.Current)
                ).take(LibraryWidget.MAX_ITEM_COUNT)
                libraryEntries.clear()
                libraryEntries.addAll(entries)
            } catch (e: Exception) {
                logE("Failed to fetch library entries for widget $appWidgetId", e)
            }
        }
    }
}