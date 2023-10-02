package io.github.drumber.kitsune.ui.base

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.chibatching.kotpref.livedata.asLiveData
import com.google.android.material.color.DynamicColors
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.main.MainActivity
import io.github.drumber.kitsune.util.extensions.clearLightNavigationBar
import io.github.drumber.kitsune.util.extensions.setStatusBarColorRes

abstract class BaseActivity(
    @LayoutRes contentLayoutId: Int,
    private val edgeToEdge: Boolean = true,
    private val updateSystemUiColors: Boolean = true,
    private val setAppTheme: Boolean = true
) : AppCompatActivity(contentLayoutId) {

    private lateinit var appliedTheme: AppTheme
    private var isDynamicColorsApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        if (setAppTheme) {
            // apply app theme
            appliedTheme = KitsunePref.appTheme
            setTheme(appliedTheme.themeRes)
            if (DynamicColors.isDynamicColorAvailable() && KitsunePref.useDynamicColorTheme) {
                DynamicColors.applyToActivityIfAvailable(this)
                isDynamicColorsApplied = true
            }
        }

        super.onCreate(savedInstanceState)

        KitsunePref.asLiveData(KitsunePref::appTheme).observe(this) {
            checkAppTheme()
        }
        KitsunePref.asLiveData(KitsunePref::useDynamicColorTheme).observe(this) {
            checkAppTheme()
        }

        // get surface color
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        // set app bar color in recent apps overview
        setAppTaskColor(typedValue.data)

        if (edgeToEdge) {
            initEdgeToEdge()
        } else {
            clearLightNavigationBar()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAppTheme()
    }

    fun startNewMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun checkAppTheme() {
        if (!setAppTheme) return
        if (appliedTheme != KitsunePref.appTheme || isDynamicColorsApplied != KitsunePref.useDynamicColorTheme) {
            recreate()
        }
    }

    private fun initEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!updateSystemUiColors) return

        setStatusBarColorRes(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= 27) {
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
                setTaskDescription(
                    ActivityManager.TaskDescription(
                        it.label,
                        R.mipmap.ic_launcher,
                        color
                    )
                )
            } else {
                val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                setTaskDescription(ActivityManager.TaskDescription(it.label, icon, color))
            }
        }
    }

}