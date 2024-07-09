package io.github.drumber.kitsune.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.navigation.NavDeepLinkBuilder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.LibraryWidget
import io.github.drumber.kitsune.domain.library.FetchLibraryEntriesForWidgetUseCase
import io.github.drumber.kitsune.ui.details.DetailsFragmentArgs
import io.github.drumber.kitsune.ui.main.MainActivity
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

            val itemClickedPendingIntent = Intent(context, KitsuneWidgetProvider::class.java).run {
                action = ACTION_ITEM_CLICKED
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                PendingIntent.getBroadcast(
                    context,
                    0,
                    this,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            }
            views.setPendingIntentTemplate(R.id.widget_list_view, itemClickedPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ITEM_CLICKED) {
            val pendingIntent = createActionToMediaDetails(context, intent)
            pendingIntent?.send()
        }
        super.onReceive(context, intent)
    }

    private fun createActionToMediaDetails(context: Context, intent: Intent): PendingIntent? {
        val mediaId = intent.getStringExtra(EXTRA_MEDIA_ID)
        val mediaType = intent.getStringExtra(EXTRA_MEDIA_TYPE)

        if (mediaType != null && mediaId != null) {
            val args = DetailsFragmentArgs(
                media = null,
                type = mediaType,
                slug = mediaId
            )

            return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.main_nav_graph)
                .setDestination(R.id.details_fragment)
                .setArguments(args.toBundle())
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()
        }
        return null
    }

    companion object {
        const val ACTION_ITEM_CLICKED =
            "io.github.drumber.kitsune.librarywidget.ACTION_ITEM_CLICKED"
        const val ACTION_PROGRESSED = "io.github.drumber.kitsune.librarywidget.ACTION_PROGRESSED"
        const val EXTRA_MEDIA_ID = "io.github.drumber.kitsune.librarywidget.EXTRA_MEDIA_ID"
        const val EXTRA_MEDIA_TYPE = "io.github.drumber.kitsune.librarywidget.EXTRA_MEDIA_TYPE"
        const val EXTRA_LIBRARY_ENTRY_ID =
            "io.github.drumber.kitsune.librarywidget.EXTRA_LIBRARY_ENTRY_ID"
    }
}