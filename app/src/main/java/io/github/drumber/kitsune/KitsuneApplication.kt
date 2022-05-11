package io.github.drumber.kitsune

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import by.kirich1409.viewbindingdelegate.ViewBindingPropertyDelegate
import com.algolia.instantsearch.telemetry.Telemetry
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.data.manager.GitHubUpdateChecker
import io.github.drumber.kitsune.data.repository.AuthRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.di.appModule
import io.github.drumber.kitsune.notification.NotificationChannels
import io.github.drumber.kitsune.notification.Notifications
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
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

        Kotpref.init(this)

        ViewBindingPropertyDelegate.strictMode = false

        KitsunePref.asLiveData(KitsunePref::darkMode).observeForever {
            AppCompatDelegate.setDefaultNightMode(it.toInt())
        }

        // opt out of algolia telemetry
        Telemetry.shared.enabled = false

        initLoggedInUser()

        if (KitsunePref.checkForUpdatesOnStart) {
            checkForNewVersion()
        }
    }

    private fun initLoggedInUser() {
        val userRepository: UserRepository by inject()
        val authRepository: AuthRepository by inject()
        if (userRepository.hasUser || authRepository.isLoggedIn) {
            applicationScope.launch(Dispatchers.IO) {
                userRepository.updateUserCache()
            }
        }
    }

    private fun checkForNewVersion() {
        applicationScope.launch {
            val updateChecker: GitHubUpdateChecker = get()
            val result = updateChecker.checkForUpdates()
            if (result is GitHubUpdateChecker.UpdateCheckerResult.NewVersion) {
                Notifications.showNewVersion(this@KitsuneApplication, result.release)
            }
        }
    }

}