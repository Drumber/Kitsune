package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.IntentAction.OPEN_LIBRARY
import io.github.drumber.kitsune.constants.IntentAction.OPEN_MEDIA
import io.github.drumber.kitsune.constants.LibraryWidget
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.media.identifier
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryProgressUseCase
import io.github.drumber.kitsune.ui.details.DetailsFragmentArgs
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelFutureOnCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LibraryAppWidget : GlanceAppWidget(), KoinComponent {

    private val libraryRepository: LibraryRepository by inject()
    private val updateLibraryEntryProgress: UpdateLibraryEntryProgressUseCase by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val initialEntries = loadData()
        provideContent {
            val scope = rememberCoroutineScope()
            val entries by getDataFlow().collectAsState(initial = initialEntries)

            GlanceTheme {
                Scaffold(horizontalPadding = 0.dp) {
                    WidgetContent(
                        entries = entries,
                        clickItemAction = { libraryEntry ->
                            val intent = getMainActivityIntent(context).apply {
                                action = OPEN_MEDIA
                                val args = DetailsFragmentArgs(
                                    media = libraryEntry.media?.toMediaDto(),
                                    type = libraryEntry.media?.mediaType?.identifier,
                                    slug = libraryEntry.media?.id
                                )
                                putExtras(args.toBundle())
                            }
                            actionStartActivity(intent)
                        },
                        progressAction = { libraryEntry, progress ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    updateLibraryEntryProgress(libraryEntry, progress)
                                }
                                updateAll(context)
                            }
                        },
                        emptyListAction = {
                            val intent = getMainActivityIntent(context).apply {
                                action = OPEN_LIBRARY
                            }
                            actionStartActivity(intent)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetContent(
        entries: List<LibraryEntryWithModification>,
        clickItemAction: (LibraryEntry) -> Action,
        progressAction: (LibraryEntry, Int) -> Unit,
        emptyListAction: () -> Action
    ) {
        if (entries.isNotEmpty()) {
            LazyColumn(
                modifier = GlanceModifier
                    .fillMaxSize()
            ) {
                items(items = entries, itemId = { item -> item.id.toLong() }) { item ->
                    LibraryItem(
                        item = item,
                        clickItemAction = clickItemAction,
                        progressAction = progressAction
                    )
                }
            }
        } else {
            EmptyView(emptyListAction)
        }
    }

    @Composable
    private fun LibraryItem(
        item: LibraryEntryWithModification,
        clickItemAction: (LibraryEntry) -> Action,
        progressAction: (LibraryEntry, Int) -> Unit
    ) {
        val context = LocalContext.current

        val posterUrl = item.media?.posterImageUrl
        var posterImage by remember(posterUrl) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(posterUrl) {
            try {
                posterImage = loadBitmap(context, posterUrl)
            } catch (e: Exception) {
                logE("Failed to load poster image for URL $posterUrl", e)
            }
        }

        val imageProvider = posterImage?.let { ImageProvider(it) }
            ?: ImageProvider(R.drawable.ic_insert_photo_48)

        Row(
            modifier = GlanceModifier.clickable(clickItemAction(item.libraryEntry))
        ) {
            Image(
                provider = imageProvider,
                contentDescription = null,
                modifier = GlanceModifier.size(width = 60.dp, height = 85.dp)
            )
            Box(contentAlignment = Alignment.BottomEnd) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = item.media?.title ?: "",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = item.media?.subtypeFormatted ?: "",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                if (item.hasEpisodesCount && item.hasStartedWatching) {
                    val episodeCount = item.episodeCount?.coerceAtLeast(1) ?: 1
                    val progress = item.progress ?: 0
                    LinearProgressIndicator(
                        progress = progress.toFloat() / episodeCount,
                        color = GlanceTheme.colors.primary,
                        backgroundColor = GlanceTheme.colors.secondaryContainer,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .cornerRadius(0)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = GlanceModifier.padding(bottom = 12.dp, end = 12.dp)
                ) {
                    val progressText = when (item.progress) {
                        null, 0 -> context.getString(R.string.library_not_started)
                        else -> "${item.progress ?: 0}/${item.episodeCountFormatted}"
                    }
                    Text(
                        text = progressText,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    SquareIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_add_24),
                        contentDescription = null,
                        modifier = GlanceModifier.size(48.dp),
                        onClick = { progressAction(item.libraryEntry, item.progress?.plus(1) ?: 1) }
                    )
                }
            }
        }
    }

    @Composable
    fun EmptyView(action: () -> Action) {
        val context = LocalContext.current
        Column(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier.fillMaxSize().padding(4.dp)
        ) {
            Text(
                text = context.getString(R.string.widget_empty_text),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(GlanceModifier.height(10.dp))
            Button(
                text = context.getString(R.string.widget_empty_action),
                onClick = action()
            )
        }
    }

    private suspend fun loadData(): List<LibraryEntryWithModification> {
        return try {
            libraryRepository.getLibraryEntriesWithModificationsByStatus(
                listOf(LibraryStatus.Current)
            ).take(LibraryWidget.MAX_ITEM_COUNT)
        } catch (e: Exception) {
            logE("Failed to get library entries.", e)
            emptyList()
        }
    }

    private fun getDataFlow(): Flow<List<LibraryEntryWithModification>> {
        return try {
            libraryRepository.getLibraryEntriesWithModificationsByStatusAsFlow(
                listOf(LibraryStatus.Current)
            ).map { it.take(LibraryWidget.MAX_ITEM_COUNT) }
        } catch (e: Exception) {
            logE("Failed to get library entries flow.", e)
            emptyFlow()
        }
    }

    private suspend fun loadBitmap(
        context: Context,
        url: String?
    ) = suspendCancellableCoroutine { cont ->
        val request = Glide.with(context)
            .asBitmap()
            .load(url)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    cont.resumeWithException(e ?: Exception("Image failed to load."))
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    cont.resume(resource)
                    return false
                }
            })
            .submit()
        cont.cancelFutureOnCancellation(request)
    }

    private fun getMainActivityIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
    }
}