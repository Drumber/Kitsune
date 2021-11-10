package io.github.drumber.kitsune

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import by.kirich1409.viewbindingdelegate.ViewBindingPropertyDelegate
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.data.repository.AuthRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.di.appModule
import io.github.drumber.kitsune.preference.KitsunePref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KitsuneApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KitsuneApplication)
            modules(appModule)
        }

        Kotpref.init(this)

        ViewBindingPropertyDelegate.strictMode = false

        KitsunePref.asLiveData(KitsunePref::darkMode).observeForever {
            AppCompatDelegate.setDefaultNightMode(it.toInt())
        }

        initLoggedInUser()
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

}