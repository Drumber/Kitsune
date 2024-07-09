package io.github.drumber.kitsune.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.LibraryWidget
import io.github.drumber.kitsune.domain.library.FetchLibraryEntriesForWidgetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KitsuneWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val fetchLibraryEntriesForWidget: FetchLibraryEntriesForWidgetUseCase by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            fetchLibraryEntriesForWidget(LibraryWidget.MAX_ITEM_COUNT)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
        }

        appWidgetIds.forEach { appWidgetId ->
            val intent = Intent(context, LibraryWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val views = RemoteViews(context.packageName, R.layout.widget_lirbary_collection).apply {
                setRemoteAdapter(R.id.widget_list_view, intent)
                setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}