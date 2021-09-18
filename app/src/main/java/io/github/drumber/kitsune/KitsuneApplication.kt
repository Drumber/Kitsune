package io.github.drumber.kitsune

import android.app.Application
import by.kirich1409.viewbindingdelegate.ViewBindingPropertyDelegate
import com.chibatching.kotpref.Kotpref
import io.github.drumber.kitsune.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KitsuneApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KitsuneApplication)
            modules(appModule)
        }

        Kotpref.init(this)

        ViewBindingPropertyDelegate.strictMode = false
    }

}