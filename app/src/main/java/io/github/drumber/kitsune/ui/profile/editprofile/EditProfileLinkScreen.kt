package io.github.drumber.kitsune.ui.profile.editprofile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId

/**
 * Compose body for [EditProfileLinkBottomSheet].
 *
 * Shows the site logo + name, a URL input field, and Cancel / Confirm / Delete buttons.
 * The host fragment is responsible for issuing fragment results and calling dismiss().
 */
@Composable
fun EditProfileLinkScreen(
    profileLinkEntry: ProfileLinkEntry,
    isCreatingNew: Boolean,
    onConfirm: (url: String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf(profileLinkEntry.url) }

    val isConfirmEnabled = url.isNotBlank() && url != profileLinkEntry.url

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        // Header: title + delete button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    if (isCreatingNew) R.string.action_add_profile_link
                    else R.string.action_edit_profile_link
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (!isCreatingNew) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete_profile_link)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Site logo + name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(getProfileSiteLogoResourceId(profileLinkEntry.site.name)),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = profileLinkEntry.site.name.orEmpty())
        }

        Spacer(modifier = Modifier.height(10.dp))

        // URL input
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text(stringResource(R.string.profile_link_url)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onConfirm(url) },
                enabled = isConfirmEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(
                        if (isCreatingNew) R.string.action_add else R.string.action_update
                    )
                )
            }
        }
    }
}
