package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val horizontalDialogPadding = PaddingValues(
        start = DialogPadding.calculateStartPadding(LocalLayoutDirection.current),
        end = DialogPadding.calculateEndPadding(LocalLayoutDirection.current)
    )

    val contentScrollState = rememberScrollState()

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties
    ) {
        Surface(
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(
                    top = DialogPadding.calculateTopPadding(),
                    bottom = DialogPadding.calculateBottomPadding()
                )
            ) {
                // Icon
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(
                            modifier = Modifier
                                .padding(horizontalDialogPadding)
                                .padding(IconPadding)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
                // Title
                title?.let {
                    CompositionLocalProvider(
                        LocalContentColor provides titleContentColor,
                        LocalTextStyle provides MaterialTheme.typography.headlineSmall
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontalDialogPadding)
                                .padding(TitlePadding)
                                .align(
                                    if (icon == null) Alignment.Start
                                    else Alignment.CenterHorizontally
                                )
                        ) {
                            title()
                        }
                    }
                }
                // Top content scroll divider
                if (contentScrollState.canScrollBackward) {
                    HorizontalDivider()
                }
                // Content
                CompositionLocalProvider(
                    LocalContentColor provides textContentColor,
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium
                ) {
                    Box(
                        Modifier
                            .weight(weight = 1f, fill = false)
                            .verticalScroll(contentScrollState)
                            .align(Alignment.Start)
                    ) {
                        content(horizontalDialogPadding)
                    }
                }
                // Bottom content scroll divider
                if (contentScrollState.canScrollForward) {
                    HorizontalDivider()
                }
                // Buttons
                Box(
                    modifier = Modifier
                        .padding(horizontalDialogPadding)
                        .align(Alignment.End)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.primary,
                        LocalTextStyle provides MaterialTheme.typography.labelLarge,
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.End
                        ) {
                            dismissButton?.invoke()
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

private val DialogPadding = PaddingValues(all = 24.dp)
private val IconPadding = PaddingValues(bottom = 16.dp)
private val TitlePadding = PaddingValues(bottom = 16.dp)
