package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chibatching.kotpref.livedata.asLiveData
import com.google.android.material.color.DynamicColors
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.domain.work.UpdateLibraryWidgetUseCase
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import org.koin.android.ext.android.inject

class AppearanceFragment : Fragment() {

    private val updateLibraryWidget: UpdateLibraryWidgetUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val useDynamicColorTheme by KitsunePref.asLiveData(KitsunePref::useDynamicColorTheme)
            .collectAsStateWithLifecycle(KitsunePref.useDynamicColorTheme)
        val appTheme by KitsunePref.asLiveData(KitsunePref::appTheme)
            .collectAsStateWithLifecycle(KitsunePref.appTheme)
        val darkMode by KitsunePref.asLiveData(KitsunePref::darkMode)
            .collectAsStateWithLifecycle(KitsunePref.darkMode)
        val oledBlackMode by KitsunePref.asLiveData(KitsunePref::oledBlackMode)
            .collectAsStateWithLifecycle(KitsunePref.oledBlackMode)
        val mediaItemSize by KitsunePref.asLiveData(KitsunePref::mediaItemSize)
            .collectAsStateWithLifecycle(KitsunePref.mediaItemSize)

        AppearanceScreen(
            uiState = AppearanceUiState(
                isDynamicColorAvailable = DynamicColors.isDynamicColorAvailable(),
                useDynamicColorTheme = useDynamicColorTheme,
                appTheme = appTheme,
                darkMode = darkMode,
                oledBlackMode = oledBlackMode,
                mediaItemSize = mediaItemSize
            ),
            onNavigateUp = { findNavController().navigateUp() },
            onDynamicColorToggle = { enabled ->
                KitsunePref.useDynamicColorTheme = enabled
                updateLibraryWidget(requireContext())
            },
            onThemeSelected = { theme ->
                KitsunePref.appTheme = theme
                updateLibraryWidget(requireContext())
            },
            onDarkModeSelected = { value ->
                if (KitsunePref.darkMode != value) {
                    KitsunePref.darkMode = value
                    AppCompatDelegate.setDefaultNightMode(value.toInt())
                }
            },
            onOledBlackToggle = { enabled ->
                KitsunePref.oledBlackMode = enabled
            },
            onMediaItemSizeSelected = { size ->
                KitsunePref.mediaItemSize = size
            }
        )
    }
}