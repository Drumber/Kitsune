package io.github.drumber.kitsune.ui.library_new

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

class NewLibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val useDynamicColorTheme by KitsunePref.asLiveData(KitsunePref::useDynamicColorTheme)
                    .asFlow()
                    .collectAsState(initial = KitsunePref.useDynamicColorTheme)
                val darkModePreference by KitsunePref.asLiveData(KitsunePref::darkMode)
                    .asFlow()
                    .collectAsState(initial = KitsunePref.darkMode)

                val isDarkModeEnabled = when (darkModePreference.toInt()) {
                    AppCompatDelegate.MODE_NIGHT_NO -> false
                    AppCompatDelegate.MODE_NIGHT_YES -> true
                    else -> isSystemInDarkTheme()
                }

                KitsuneTheme(dynamicColor = useDynamicColorTheme, darkTheme = isDarkModeEnabled) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = { LibraryTopBar() }
                    ) { innerPadding ->
                        LibraryContent(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}