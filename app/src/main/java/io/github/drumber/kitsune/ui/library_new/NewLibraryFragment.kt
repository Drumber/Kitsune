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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.asFlow
import androidx.navigation.findNavController
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewLibraryFragment : Fragment() {

    private val viewModel: NewLibraryViewModel by viewModel()

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
                        topBar = { LibraryTopBar(modifier = Modifier.padding(horizontal = 8.dp)) }
                    ) { innerPadding ->
                        LibraryContent(
                            modifier = Modifier.padding(innerPadding),
                            currentLibraryEntries = viewModel.currentLibraryEntriesPager,
                            onItemClick = { libraryEntry ->
                                val mediaDto =
                                    libraryEntry.media?.toMediaDto() ?: return@LibraryContent
                                val action =
                                    NewLibraryFragmentDirections.actionNewLibraryFragmentToDetailsFragment(
                                        mediaDto
                                    )
                                findNavController().navigate(action)
                            },
                            onEditClick = { libraryEntry ->
                                val action =
                                    NewLibraryFragmentDirections.actionNewLibraryFragmentToLibraryEditEntryFragment(
                                        libraryEntry.id
                                    )
                                findNavController().navigate(action)
                            },
                            onRatingClick = { libraryEntry ->
                                val media = libraryEntry.media
                                // TODO: handle fragment result and update library entry
                                val action =
                                    NewLibraryFragmentDirections.actionNewLibraryFragmentToRatingBottomSheet(
                                        title = media?.title ?: "",
                                        ratingTwenty = libraryEntry.ratingTwenty ?: -1,
                                        ratingResultKey = "TODO",
                                        removeResultKey = "TODO",
                                        ratingSystem = RatingSystemUtil.getRatingSystem()
                                    )
                                findNavController().navigate(action)
                            },
                            onIncrementProgress = { libraryEntryWithModification ->
                                viewModel.incrementProgress(libraryEntryWithModification)
                            }
                        )
                    }
                }
            }
        }
    }
}