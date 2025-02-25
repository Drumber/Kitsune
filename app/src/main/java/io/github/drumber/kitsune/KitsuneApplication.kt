package io.github.drumber.kitsune

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.algolia.instantsearch.core.InstantSearchTelemetry
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.data.presentation.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.repository.AppUpdateRepository
import io.github.drumber.kitsune.di.appModule
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.domain.user.UpdateLocalUserUseCase
import io.github.drumber.kitsune.notification.NotificationChannels
import io.github.drumber.kitsune.notification.Notifications
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import io.github.drumber.kitsune.util.logW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KitsuneApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        try {
            NotificationChannels.registerNotificationChannels(this)
        } catch (e: Exception) {
            logE("Failed to register notification channels.", e)
        }

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO)
            androidContext(this@KitsuneApplication)
            modules(appModule)
        }

        performMigrations()

        Kotpref.init(this)

        KitsunePref.asLiveData(KitsunePref::darkMode).observeForever {
            AppCompatDelegate.setDefaultNightMode(it.toInt())
        }

        // opt out of algolia telemetry
        InstantSearchTelemetry.shared.enabled = false

        initLoggedInUser()

        if (!BuildConfig.SCREENSHOT_MODE_ENABLED && KitsunePref.checkForUpdatesOnStart) {
            checkForNewVersion()
        }
    }

    private fun performMigrations() {
        // 1.8.0 - replaced ResourceDatabase with LocalDatabase
        listOf("resources.db", "resources.db-shm", "resources.db-wal").forEach {
            val databaseFile = getDatabasePath(it)
            if (databaseFile.isFile) {
                try {
                    val isDeleted = databaseFile.delete()
                    if (isDeleted)
                        logI("[Migration-1.8.0] Deleted database file '${databaseFile.absolutePath}'.")
                    else
                        logW("[Migration-1.8.0] Failed to delete database file '${databaseFile.absolutePath}'.")
                } catch (e: Exception) {
                    logE(
                        "[Migration-1.8.0] Error while deleting database file '${databaseFile.absolutePath}'.",
                        e
                    )
                }
            }
        }
    }

    private fun initLoggedInUser() {
        val isUserLoggedIn: IsUserLoggedInUseCase by inject()
        if (isUserLoggedIn()) {
            val updateLocalUser: UpdateLocalUserUseCase by inject()
            applicationScope.launch(Dispatchers.IO) {
                updateLocalUser()
            }
        }
    }

    private fun checkForNewVersion() {
        applicationScope.launch {
            val updateChecker: AppUpdateRepository = get()
            val result = updateChecker.checkForUpdates()
            if (result is UpdateCheckResult.NewVersion) {
                Notifications.showNewVersion(this@KitsuneApplication, result.release)
            }
        }
    }
}