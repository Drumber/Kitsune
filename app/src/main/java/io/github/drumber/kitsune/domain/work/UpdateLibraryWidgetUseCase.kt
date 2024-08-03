package io.github.drumber.kitsune.domain.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.github.drumber.kitsune.work.UpdateLibraryWidgetWorker

class UpdateLibraryWidgetUseCase {

    operator fun invoke(context: Context) {
        val updateWidgetWork = OneTimeWorkRequestBuilder<UpdateLibraryWidgetWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UpdateLibraryWidgetWorker.TAG,
            ExistingWorkPolicy.REPLACE,
            updateWidgetWork
        )
    }
}