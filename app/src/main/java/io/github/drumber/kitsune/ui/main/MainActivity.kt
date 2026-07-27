package io.github.drumber.kitsune.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.IntentAction.OPEN_LIBRARY
import io.github.drumber.kitsune.constants.IntentAction.OPEN_MEDIA
import io.github.drumber.kitsune.constants.IntentAction.SHORTCUT_LIBRARY
import io.github.drumber.kitsune.constants.IntentAction.SHORTCUT_SEARCH
import io.github.drumber.kitsune.constants.IntentAction.SHORTCUT_SETTINGS
import io.github.drumber.kitsune.domain.work.UpdateLibraryWidgetUseCase
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.preference.StartPagePref
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.ui.navigation.KitsuneApp
import io.github.drumber.kitsune.ui.navigation.Routes
import io.github.drumber.kitsune.ui.navigation.navigateSafe
import io.github.drumber.kitsune.ui.navigation.navigateToTopLevel
import io.github.drumber.kitsune.ui.onboarding.OnboardingActivity
import io.github.drumber.kitsune.ui.permissions.requestNotificationPermission
import io.github.drumber.kitsune.ui.permissions.showNotificationPermissionRejectedDialog
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by viewModel()

    private val updateLibraryWidget by inject<UpdateLibraryWidgetUseCase>()

    private var navController: NavHostController? = null

    /** Intent whose action still has to be handled once the nav host is composed. */
    private var pendingIntent by mutableStateOf<Intent?>(null)

    private var handledIntentHashCode: Int? = null

    private var backPressedToExitTime = 0L
    private var exitToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reLoginPrompt.collectLatest { promptUserReLogin() }
            }
        }

        val initialLoginState = viewModel.isLoggedIn()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoggedInFlow.collectLatest { isLoggedIn ->
                    if (initialLoginState != isLoggedIn) {
                        updateLibraryWidget(this@MainActivity)
                        startNewMainActivity()
                    }
                }
            }
        }

        handledIntentHashCode = savedInstanceState
            ?.takeIf { it.containsKey(LAST_HANDLED_INTENT_KEY) }
            ?.getInt(LAST_HANDLED_INTENT_KEY)

        // The graph start destination has to stay Home for the whole session: `navigateToTopLevel`
        // pops up to it, and a restored back stack keeps the root it was created with. A configured
        // start page or an app shortcut is therefore an explicit navigation on top of Home.
        val initialRoute = if (savedInstanceState == null) resolveInitialRoute() else null

        setContent {
            val isDarkModeEnabled = when (KitsunePref.darkMode.toInt()) {
                AppCompatDelegate.MODE_NIGHT_NO -> false
                AppCompatDelegate.MODE_NIGHT_YES -> true
                else -> isSystemInDarkTheme()
            }
            KitsuneTheme(
                darkTheme = isDarkModeEnabled,
                dynamicColor = KitsunePref.useDynamicColorTheme,
                variant = KitsunePref.appTheme,
                amoled = KitsunePref.oledBlackMode
            ) {
                val controller = rememberNavController()
                navController = controller
                val localUser by viewModel.localUser.collectAsStateWithLifecycle(initialValue = null)

                KitsuneApp(
                    avatarUrl = localUser?.avatar?.originalOrDown(),
                    startDestination = Routes.Home,
                    navController = controller,
                    doubleBackToExit = KitsunePref.doubleBackToExit,
                    onExitRequested = ::handleBackPressToExit
                )

                LaunchedEffect(initialRoute) {
                    if (initialRoute != null) {
                        controller.navigateToTopLevel(initialRoute)
                    }
                }

                val intentToHandle = pendingIntent
                LaunchedEffect(intentToHandle) {
                    if (intentToHandle != null) {
                        handleIntentAction(intentToHandle, controller)
                        pendingIntent = null
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            onCreateWithoutSavedInstanceState()
        }
    }

    private fun onCreateWithoutSavedInstanceState() {
        if (shouldStartOnboarding()) {
            startOnboardingActivity()
        } else if (KitsunePref.checkForUpdatesOnStart) {
            requestRequiredPermissions()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        handledIntentHashCode?.let { outState.putInt(LAST_HANDLED_INTENT_KEY, it) }
    }

    override fun onStart() {
        super.onStart()
        pendingIntent = intent
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val controller = navController
        if (controller == null || !controller.handleDeepLink(intent)) {
            pendingIntent = intent
        }
    }

    /**
     * Route to open on top of [Routes.Home] on a clean launch: an app shortcut wins over the user's
     * configured start page, and neither applies when the app was opened through a deep link.
     * `null` means staying on Home.
     */
    private fun resolveInitialRoute(): Any? {
        if (isLaunchedByDeepLink()) return null

        shortcutStartDestination()?.let { return it }

        return when (KitsunePref.startFragment) {
            StartPagePref.Home -> null
            StartPagePref.Search -> Routes.Search()
            StartPagePref.Library -> Routes.Library
            StartPagePref.Profile -> Routes.MyProfile
        }
    }

    private fun shortcutStartDestination(): Any? = when (intent.action) {
        SHORTCUT_LIBRARY -> Routes.Library
        SHORTCUT_SEARCH -> Routes.Search(focusSearch = true)
        SHORTCUT_SETTINGS -> Routes.SettingsGraph
        else -> null
    }

    private fun handleIntentAction(intent: Intent, controller: NavHostController): Boolean {
        if (handledIntentHashCode == intent.filterHashCode()) return false
        handledIntentHashCode = intent.filterHashCode()

        return when (intent.action) {
            OPEN_MEDIA -> {
                val mediaId = intent.getStringExtra(EXTRA_MEDIA_ID)
                if (mediaId.isNullOrBlank()) {
                    false
                } else {
                    controller.navigate(
                        Routes.Details(
                            mediaId = mediaId,
                            isAnime = intent.getBooleanExtra(EXTRA_MEDIA_IS_ANIME, true)
                        )
                    )
                    true
                }
            }

            OPEN_LIBRARY -> {
                controller.navigateToTopLevel(Routes.Library)
                true
            }

            else -> false
        }
    }

    private fun handleBackPressToExit() {
        if (System.currentTimeMillis() - backPressedToExitTime < BACK_PRESS_EXIT_INTERVAL_MS) {
            exitToast?.cancel()
            finish()
        } else {
            backPressedToExitTime = System.currentTimeMillis()
            exitToast = Toast.makeText(this, R.string.press_back_again_to_exit, Toast.LENGTH_SHORT)
                .also { it.show() }
        }
    }

    private fun requestRequiredPermissions() {
        if (!KitsunePref.flagUserDeniedNotificationPermission) {
            val requestNotificationPermissionLauncher =
                registerForActivityResult(RequestPermission()) { isGranted ->
                    if (isGranted) {
                        KitsunePref.flagUserDeniedNotificationPermission = false
                    } else {
                        KitsunePref.checkForUpdatesOnStart = false
                        KitsunePref.flagUserDeniedNotificationPermission = true
                        showNotificationPermissionRejectedDialog()
                    }
                }
            requestNotificationPermission(requestNotificationPermissionLauncher) {
                KitsunePref.flagUserDeniedNotificationPermission = true
            }
        }
    }

    private fun promptUserReLogin() {
        navController?.navigateSafe(Routes.Login(wasLoggedOut = true))
    }

    private fun startNewMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun shouldStartOnboarding(): Boolean {
        return !BuildConfig.INSTRUMENTED_TEST && KitsunePref.onboardingFinishedVersionCode == -1
    }

    private fun startOnboardingActivity() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun isLaunchedByDeepLink(): Boolean {
        return intent.action == Intent.ACTION_VIEW && intent.data != null
    }

    companion object {
        private const val LAST_HANDLED_INTENT_KEY = "last_handled_intent"
        private const val BACK_PRESS_EXIT_INTERVAL_MS = 2000L

        /** Extras used by the library widget to open a media details screen. */
        const val EXTRA_MEDIA_ID = "mediaId"
        const val EXTRA_MEDIA_IS_ANIME = "isAnime"
    }
}
