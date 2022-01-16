package io.github.drumber.kitsune.ui.base

import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.extensions.clearLightNavigationBar
import io.github.drumber.kitsune.util.extensions.setStatusBarColor

abstract class BaseActivity(
    @LayoutRes contentLayoutId: Int,
    private val edgeToEdge: Boolean = true,
    private val updateSystemUiColors: Boolean = true
) : AppCompatActivity(contentLayoutId) {

    private lateinit var appliedTheme: AppTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        // apply app theme
        appliedTheme = KitsunePref.appTheme
        setTheme(appliedTheme.themeRes)

        super.onCreate(savedInstanceState)

        KitsunePref.asLiveData(KitsunePref::appTheme).observe(this) {
            checkAppTheme()
        }

        // get surface color
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        // set app bar color in recent apps overview
        setAppTaskColor(typedValue.data)

        if(edgeToEdge) {
            initEdgeToEdge()
        } else {
            clearLightNavigationBar()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAppTheme()
    }

    private fun checkAppTheme() {
        if (appliedTheme != KitsunePref.appTheme) {
            recreate()
        }
    }

    private fun initEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!updateSystemUiColors) return

        setStatusBarColor(ContextCompat.getColor(this, R.color.translucent_status_bar))
        if(Build.VERSION.SDK_INT >= 27) {
            window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        }
    }

    /**
     * Change the color of the app bar that is visible in the recent
     * app overview. This does only change the color for the current
     * activity task.
     * @param color     the new color that should be applied
     */
    private fun setAppTaskColor(color: Int) {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // find the app task that corresponds to this activity
        val appTask = activityManager.appTasks.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.taskInfo.taskId == taskId
            } else {
                it.taskInfo.id == taskId
            }
        }
        // change the color of the task description, but keep the label and app icon
        appTask?.taskInfo?.taskDescription?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setTaskDescription(ActivityManager.TaskDescription(it.label, R.mipmap.ic_launcher, color))
            } else {
                val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                setTaskDescription(ActivityManager.TaskDescription(it.label, icon, color))
            }
        }
    }

}