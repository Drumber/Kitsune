package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    localUser: LocalUser? = null,
    onLoginClicked: () -> Unit = {},
    onCreateAccountClicked: () -> Unit = {},
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val backgroundGradient = listOf(
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surface
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(backgroundGradient))
            .then(modifier)
            .padding(16.dp)
    ) {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                HeaderSection()
                Spacer(modifier = Modifier.height(32.dp))
                if (localUser != null) {
                    LoggedInUserSection(
                        modifier = Modifier.fillMaxWidth(),
                        localUser = localUser,
                        onNextClicked = onNext
                    )
                } else {
                    ActionSection(
                        onLoginClicked = onLoginClicked,
                        onCreateAccountClicked = onCreateAccountClicked
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.weight(1f)
            ) {
                HeaderSection(modifier = Modifier.fillMaxWidth(0.5f))
                Spacer(modifier = Modifier.width(32.dp))
                Column {
                    if (localUser != null) {
                        LoggedInUserSection(
                            modifier = Modifier.fillMaxWidth(),
                            localUser = localUser,
                            onNextClicked = onNext
                        )
                    } else {
                        ActionSection(
                            onLoginClicked = onLoginClicked,
                            onCreateAccountClicked = onCreateAccountClicked
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OnboardingNavigationControls(
            hideNextButton = localUser != null,
            onBackClicked = onBack,
            onNextClicked = onNext,
            nextText = stringResource(R.string.action_skip)
        )
    }
}

@Composable
private fun HeaderSection(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.weight(1f, false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.onboarding_login_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(148.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_login_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LoggedInUserSection(
    modifier: Modifier = Modifier,
    localUser: LocalUser,
    onNextClicked: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Card(
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                GlideImage(
                    model = localUser.avatar?.originalOrDown(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    it.placeholder(R.drawable.profile_picture_placeholder)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        text = localUser.name ?: stringResource(R.string.no_information)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = stringResource(R.string.onboarding_login_is_logged_in)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNextClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun ActionSection(
    modifier: Modifier = Modifier,
    isDisabled: Boolean = false,
    onLoginClicked: () -> Unit = {},
    onCreateAccountClicked: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            onClick = onLoginClicked,
            enabled = !isDisabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.action_log_in))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCreateAccountClicked,
            enabled = !isDisabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.action_create_account))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPagePreview() {
    KitsuneTheme {
        LoginPage(localUser = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPageAuthenticatedPreview() {
    KitsuneTheme {
        LoginPage(localUser = LocalUser.empty(""))
    }
}
