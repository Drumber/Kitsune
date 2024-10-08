package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceCard(
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.merge(MaterialTheme.typography.bodyLarge)
                ) {
                    title()
                }
                Spacer(Modifier.height(2.dp))
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    LocalTextStyle provides LocalTextStyle.current.merge(MaterialTheme.typography.bodyMedium)
                ) {
                    description()
                }
            }
            if (action != null) {
                Spacer(Modifier.width(4.dp))
                action()
            }
        }
    }
}