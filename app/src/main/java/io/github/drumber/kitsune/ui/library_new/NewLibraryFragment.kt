package io.github.drumber.kitsune.ui.library_new

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asFlow
import androidx.navigation.findNavController
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.library.RatingBottomSheet
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewLibraryFragment : Fragment() {

    private val viewModel: NewLibraryViewModel by viewModel()

    companion object {
        const val RESULT_KEY_RATING = "new_library_rating_result_key"
        const val RESULT_KEY_REMOVE_RATING = "new_library_remove_rating_result_key"
    }

    @OptIn(ExperimentalMaterial3Api::class)
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

                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

                KitsuneTheme(dynamicColor = useDynamicColorTheme, darkTheme = isDarkModeEnabled) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            LibraryTopBar(
                                modifier = Modifier.fillMaxWidth(),
                                scrollBehavior = scrollBehavior,
                                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                            )
                        }
                    ) { innerPadding ->
                        LibraryContent(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(top = 8.dp),
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
                                val action =
                                    NewLibraryFragmentDirections.actionNewLibraryFragmentToRatingBottomSheet(
                                        title = media?.title ?: "",
                                        ratingTwenty = libraryEntry.ratingTwenty ?: -1,
                                        ratingResultKey = RESULT_KEY_RATING,
                                        removeResultKey = RESULT_KEY_REMOVE_RATING,
                                        ratingSystem = RatingSystemUtil.getRatingSystem(),
                                        entryId = libraryEntry.id
                                    )
                                findNavController().navigate(action)
                            },
                            onIncrementProgress = { libraryEntryWithModification ->
                                viewModel.incrementProgress(libraryEntryWithModification)
                            },
                            onNavigateToStatus = { /* TODO */ },
                            onNavigateToWholeLibrary = {
                                findNavController().navigate(R.id.library_fragment)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(RESULT_KEY_RATING) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            val libraryEntryId = bundle.getString(RatingBottomSheet.BUNDLE_ENTRY_ID)
            if (rating != -1 && libraryEntryId != null) {
                viewModel.updateRating(libraryEntryId, rating)
            }
        }

        setFragmentResultListener(RESULT_KEY_REMOVE_RATING) { _, bundle ->
            val libraryEntryId = bundle.getString(RatingBottomSheet.BUNDLE_ENTRY_ID)
            if (libraryEntryId != null) {
                viewModel.updateRating(libraryEntryId, null)
            }
        }
    }
}