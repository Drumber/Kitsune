package io.github.drumber.kitsune.ui.profile.editprofile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.parseDate
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onDismiss: () -> Unit,
    onAvatarClick: () -> Unit,
    onCoverClick: () -> Unit,
    onBirthdayClick: (currentDateMs: Long) -> Unit,
    onAddProfileLink: () -> Unit,
    onEditProfileLink: (ProfileLinkEntry) -> Unit,
    onSaveClick: () -> Unit,
    onWaifuCharacterSearchClick: () -> Unit
) {
    val profileState by viewModel.profileStateFlow.collectAsStateWithLifecycle()
    val profileImageState by viewModel.profileImageStateFlow.collectAsStateWithLifecycle()
    val profileLinkEntries by viewModel.profileLinkEntriesFlow.collectAsStateWithLifecycle()
    val canUpdate by viewModel.canUpdateProfileFlow.collectAsStateWithLifecycle(initialValue = false)
    val loadingState by viewModel.loadingStateFlow.collectAsStateWithLifecycle()
    val isLoading = loadingState is LoadingState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_edit_profile)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                )
            }
        ) { innerPadding ->
            EditProfileForm(
                profileState = profileState,
                profileImageState = profileImageState,
                profileLinkEntries = profileLinkEntries,
                canUpdate = canUpdate,
                onAvatarClick = onAvatarClick,
                onCoverClick = onCoverClick,
                onBirthdayClick = onBirthdayClick,
                onAddProfileLink = onAddProfileLink,
                onEditProfileLink = onEditProfileLink,
                onSaveClick = onSaveClick,
                onProfileStateChange = viewModel.acceptProfileChanges,
                onWaifuCharacterSearchClick = onWaifuCharacterSearchClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalGlideComposeApi::class)
@Composable
private fun EditProfileForm(
    profileState: ProfileState,
    profileImageState: ProfileImageState,
    profileLinkEntries: List<ProfileLinkEntry>,
    canUpdate: Boolean,
    onAvatarClick: () -> Unit,
    onCoverClick: () -> Unit,
    onBirthdayClick: (Long) -> Unit,
    onAddProfileLink: () -> Unit,
    onEditProfileLink: (ProfileLinkEntry) -> Unit,
    onSaveClick: () -> Unit,
    onProfileStateChange: (ProfileState) -> Unit,
    onWaifuCharacterSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        CoverAndAvatarSection(
            profileImageState = profileImageState,
            onAvatarClick = onAvatarClick,
            onCoverClick = onCoverClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = profileState.location,
            onValueChange = { onProfileStateChange(profileState.copy(location = it)) },
            label = { Text(stringResource(R.string.profile_data_location)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        BirthdayField(
            birthday = profileState.birthday,
            onBirthdayClick = onBirthdayClick,
            onClearBirthday = { onProfileStateChange(profileState.copy(birthday = "")) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        GenderFields(
            gender = profileState.gender,
            customGender = profileState.customGender,
            onGenderChange = { newGender, newCustom ->
                onProfileStateChange(profileState.copy(gender = newGender, customGender = newCustom))
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        WaifuFields(
            waifuOrHusbando = profileState.waifuOrHusbando,
            characterName = profileState.character?.name,
            onWaifuChange = { onProfileStateChange(profileState.copy(waifuOrHusbando = it)) },
            onClearCharacter = { onProfileStateChange(profileState.copy(character = null)) },
            onCharacterSearchClick = onWaifuCharacterSearchClick
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = profileState.about,
            onValueChange = { onProfileStateChange(profileState.copy(about = it)) },
            label = { Text(stringResource(R.string.profile_data_bio)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProfileLinksSection(
            entries = profileLinkEntries,
            onAddClick = onAddProfileLink,
            onEditClick = onEditProfileLink
        )
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(
            onClick = onSaveClick,
            enabled = canUpdate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_update_profile))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CoverAndAvatarSection(
    profileImageState: ProfileImageState,
    onAvatarClick: () -> Unit,
    onCoverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverImage = profileImageState.selectedCoverUri ?: profileImageState.currentCoverUrl
    val avatarImage = profileImageState.selectedAvatarUri ?: profileImageState.currentAvatarUrl

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .clickable(onClick = onCoverClick)
    ) {
        GlideImage(
            model = coverImage,
            contentDescription = stringResource(R.string.profile_cover_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            it.placeholder(R.drawable.cover_placeholder).error(R.drawable.cover_placeholder)
        }
        Avatar(
            imageUrl = avatarImage?.toString(),
            size = 72.dp,
            contentDescription = stringResource(R.string.profile_avatar_image_description),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clickable(onClick = onAvatarClick)
        )
    }
}

@Composable
private fun BirthdayField(
    birthday: String,
    onBirthdayClick: (Long) -> Unit,
    onClearBirthday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDate = DataUtil.formatDate(birthday) ?: ""
    OutlinedTextField(
        value = displayDate,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.profile_data_birthday)) },
        trailingIcon = {
            if (birthday.isEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_month_24),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        onBirthdayClick(
                            birthday.parseDate()?.time
                                ?: com.google.android.material.datepicker.MaterialDatePicker
                                    .todayInUtcMilliseconds()
                        )
                    }
                )
            } else {
                IconButton(onClick = onClearBirthday) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onBirthdayClick(
                    birthday.parseDate()?.time
                        ?: com.google.android.material.datepicker.MaterialDatePicker
                            .todayInUtcMilliseconds()
                )
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderFields(
    gender: String,
    customGender: String,
    onGenderChange: (gender: String, customGender: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val genderOptions = listOf(
        "secret" to stringResource(R.string.profile_data_private),
        "male" to stringResource(R.string.profile_gender_male),
        "female" to stringResource(R.string.profile_gender_female),
        "custom" to stringResource(R.string.profile_gender_custom)
    )
    val displayGender = DataUtil.getGenderString(gender, context) ?: ""
    var genderExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = genderExpanded,
        onExpandedChange = { genderExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayGender,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.profile_data_gender)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
            genderOptions.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        genderExpanded = false
                        val newCustom = if (key != "custom") "" else customGender
                        onGenderChange(key, newCustom)
                    }
                )
            }
        }
    }
    if (gender == "custom") {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customGender,
            onValueChange = { onGenderChange("custom", it) },
            label = { Text(stringResource(R.string.profile_gender_custom_hint)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaifuFields(
    waifuOrHusbando: String,
    characterName: String?,
    onWaifuChange: (String) -> Unit,
    onClearCharacter: () -> Unit,
    onCharacterSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val waifuOptions = listOf(
        "" to "",
        "Waifu" to stringResource(R.string.profile_data_waifu),
        "Husbando" to stringResource(R.string.profile_data_husbando)
    )
    var waifuExpanded by remember { mutableStateOf(false) }
    val displayWaifu = waifuOptions.find { it.first == waifuOrHusbando }?.second ?: waifuOrHusbando

    ExposedDropdownMenuBox(
        expanded = waifuExpanded,
        onExpandedChange = { waifuExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayWaifu,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.profile_data_waifu)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = waifuExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = waifuExpanded,
            onDismissRequest = { waifuExpanded = false }
        ) {
            waifuOptions.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { waifuExpanded = false; onWaifuChange(key) }
                )
            }
        }
    }
    if (waifuOrHusbando.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = characterName.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(waifuOrHusbando) },
            trailingIcon = {
                if (characterName != null) {
                    IconButton(onClick = onClearCharacter) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                } else {
                    Icon(painterResource(R.drawable.ic_search_24), contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCharacterSearchClick)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileLinksSection(
    entries: List<ProfileLinkEntry>,
    onAddClick: () -> Unit,
    onEditClick: (ProfileLinkEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(modifier = modifier.fillMaxWidth()) {
        entries.sortedByDescending { it.site.id?.toIntOrNull() }.forEach { entry ->
            InputChip(
                selected = false,
                onClick = { onEditClick(entry) },
                label = { Text(entry.site.name.orEmpty()) },
                avatar = {
                    Icon(
                        painter = painterResource(getProfileSiteLogoResourceId(entry.site.name)),
                        contentDescription = null,
                        modifier = Modifier.size(InputChipDefaults.AvatarSize)
                    )
                },
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        AssistChip(
            onClick = onAddClick,
            label = { Text(stringResource(R.string.action_add_profile_link)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_add_24),
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
    }
}
