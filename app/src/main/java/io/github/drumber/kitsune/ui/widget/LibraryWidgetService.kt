package io.github.drumber.kitsune.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import io.github.drumber.kitsune.data.repository.LibraryRepository
import org.koin.android.ext.android.inject

class LibraryWidgetService : RemoteViewsService() {

    private val libraryRepository: LibraryRepository by inject()

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return LibraryRemoteViewsFactory(
            applicationContext,
            libraryRepository,
            intent
        )
    }
}