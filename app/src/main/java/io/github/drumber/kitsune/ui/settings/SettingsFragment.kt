package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.chibatching.kotpref.livedata.asLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.repository.AppUpdateRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.notification.Notifications
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.permissions.isNotificationPermissionGranted
import io.github.drumber.kitsune.ui.permissions.requestNotificationPermission
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openUrl
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    private val appUpdateRepository: AppUpdateRepository by inject()

    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    KitsunePref.flagUserDeniedNotificationPermission = false
                } else {
                    KitsunePref.flagUserDeniedNotificationPermission = true
                    Toast.makeText(
                        requireContext(),
                        R.string.error_requires_notification_permission,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { SettingsContent() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.errorMessageListener = {
            Snackbar.make(view, "Error: ${it.getMessage(requireContext())}", Snackbar.LENGTH_LONG)
                .setAction(R.string.action_dismiss) {}
                .show()
        }
    }

    override fun onDestroyView() {
        viewModel.errorMessageListener = null
        super.onDestroyView()
    }

    @Composable
    private fun SettingsContent() {
        val userState by viewModel.userModel.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
        val startFragment by KitsunePref.asLiveData(KitsunePref::startFragment)
            .collectAsStateWithLifecycle(KitsunePref.startFragment)
        val rememberSearchFilters by KitsunePref.asLiveData(KitsunePref::rememberSearchFilters)
            .collectAsStateWithLifecycle(KitsunePref.rememberSearchFilters)
        val doubleBackToExit by KitsunePref.asLiveData(KitsunePref::doubleBackToExit)
            .collectAsStateWithLifecycle(KitsunePref.doubleBackToExit)
        val forceLegacyImagePicker by KitsunePref.asLiveData(KitsunePref::forceLegacyImagePicker)
            .collectAsStateWithLifecycle(KitsunePref.forceLegacyImagePicker)
        val checkForUpdatesOnStart by KitsunePref.asLiveData(KitsunePref::checkForUpdatesOnStart)
            .collectAsStateWithLifecycle(KitsunePref.checkForUpdatesOnStart)
        val titles by KitsunePref.getTitleLanguageAsFlow()
            .collectAsStateWithLifecycle(KitsunePref.titles)
        val context = LocalContext.current
        val uiState = SettingsUiState(
            user = userState,
            isLoading = isLoading,
            titles = titles,
            startFragment = startFragment,
            rememberSearchFilters = rememberSearchFilters,
            doubleBackToExit = doubleBackToExit,
            isPhotoPickerAvailable = ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context),
            forceLegacyImagePicker = forceLegacyImagePicker,
            checkForUpdatesOnStart = checkForUpdatesOnStart,
            appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )
        SettingsScreen(uiState = uiState, callbacks = buildCallbacks(userState))
    }

    private fun buildCallbacks(user: LocalUser?): SettingsCallbacks = SettingsCallbacks(
        onNavigateUp = { findNavController().navigateUp() },
        onNavigateToAppearance = {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAppearanceFragment()
            findNavController().navigate(action)
        },
        onNavigateToAppLogs = {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAppLogsFragment()
            findNavController().navigateSafe(R.id.settingsFragment, action)
        },
        onNavigateToLibraries = {
            val action = SettingsFragmentDirections.actionSettingsFragmentToLibrariesFragment()
            findNavController().navigateSafe(R.id.settingsFragment, action)
        },
        onNavigateToGitHub = { openUrl(getString(R.string.github_repo_url)) },
        onLanguageSelected = { tag ->
            val localeList = if (tag.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(tag)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        },
        onStartFragmentSelected = { pref -> KitsunePref.startFragment = pref },
        onTitlesSelected = { pref ->
            val old = KitsunePref.titles
            KitsunePref.titles = pref
            if (user != null && old != pref) {
                viewModel.updateUser(LocalUser.empty(user.id).copy(titleLanguagePreference = pref))
            }
        },
        onCountrySelected = { code ->
            user?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(country = code)) }
        },
        onSfwFilterSelected = { pref ->
            user?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(sfwFilterPreference = pref)) }
        },
        onRatingSystemSelected = { pref ->
            user?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(ratingSystem = pref)) }
        },
        onDisplayNameChanged = { name ->
            user?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(name = name)) }
        },
        onProfileUrlChanged = { slug ->
            user?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(slug = slug)) }
        },
        onRememberSearchFiltersToggle = { KitsunePref.rememberSearchFilters = it },
        onDoubleBackToExitToggle = { KitsunePref.doubleBackToExit = it },
        onForceLegacyImagePickerToggle = { KitsunePref.forceLegacyImagePicker = it },
        onCheckForUpdatesToggle = { enabled ->
            if (enabled && !requireContext().isNotificationPermissionGranted()) {
                requireActivity().requestNotificationPermission(requestNotificationPermissionLauncher)
            } else {
                KitsunePref.checkForUpdatesOnStart = enabled
            }
        },
        onAppVersionClick = { checkForNewVersion() }
    )

    private fun checkForNewVersion() {
        Toast.makeText(
            requireContext(),
            R.string.info_update_checking_new_version,
            Toast.LENGTH_SHORT
        ).show()
        lifecycleScope.launch {
            when (val result = appUpdateRepository.checkForUpdates(BuildConfig.VERSION_NAME)) {
                is UpdateCheckResult.NewVersion -> {
                    val release = result.release
                    Notifications.showNewVersion(requireContext(), release)
                    val message = getString(
                        R.string.info_update_new_version_available_text,
                        release.version
                    )
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_view) { openUrl(release.url) }
                        .show()
                }
                is UpdateCheckResult.NoNewVersion -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.info_update_no_new_version_available,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is UpdateCheckResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        R.string.info_update_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
