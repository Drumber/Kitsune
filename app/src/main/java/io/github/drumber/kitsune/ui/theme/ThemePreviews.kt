package io.github.drumber.kitsune.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.constants.AppTheme

// ── Internal preview helper ───────────────────────────────────────────────────

/**
 * Lightweight preview wrapper that bypasses MDC attribute reading.
 * For production code always use [KitsuneTheme] instead.
 */
@Composable
private fun PreviewKitsuneTheme(
    variant: AppTheme,
    dark: Boolean,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = colorSchemeForVariant(variant, dark).let {
        if (amoled && dark) it.withAmoledSurfaces() else it
    }
    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}

// ── Preview content ───────────────────────────────────────────────────────────

@Composable
private fun ThemeSwatch(label: String) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ColorChip(MaterialTheme.colorScheme.primary, "primary")
                ColorChip(MaterialTheme.colorScheme.secondary, "secondary")
                ColorChip(MaterialTheme.colorScheme.tertiary, "tertiary")
                ColorChip(MaterialTheme.colorScheme.surface, "surface")
                ColorChip(MaterialTheme.colorScheme.background, "bg")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ColorChip(MaterialTheme.colorScheme.surfaceContainerLowest, "scl0")
                ColorChip(MaterialTheme.colorScheme.surfaceContainerLow, "scl")
                ColorChip(MaterialTheme.colorScheme.surfaceContainer, "sc")
                ColorChip(MaterialTheme.colorScheme.surfaceContainerHigh, "sch")
                ColorChip(MaterialTheme.colorScheme.surfaceContainerHighest, "schh")
            }
            Button(onClick = {}) { Text("Button") }
        }
    }
}

@Composable
private fun ColorChip(color: Color, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Text(text = name, style = MaterialTheme.typography.labelSmall)
    }
}

// ── DEFAULT previews ──────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Default – Light")
@Composable
private fun PreviewDefaultLight() {
    PreviewKitsuneTheme(variant = AppTheme.DEFAULT, dark = false) {
        ThemeSwatch("DEFAULT – Light")
    }
}

@Preview(showBackground = true, name = "Default – Dark", backgroundColor = 0xFF1A1110)
@Composable
private fun PreviewDefaultDark() {
    PreviewKitsuneTheme(variant = AppTheme.DEFAULT, dark = true) {
        ThemeSwatch("DEFAULT – Dark")
    }
}

@Preview(showBackground = true, name = "Default – AMOLED", backgroundColor = 0xFF000000)
@Composable
private fun PreviewDefaultAmoled() {
    PreviewKitsuneTheme(variant = AppTheme.DEFAULT, dark = true, amoled = true) {
        ThemeSwatch("DEFAULT – AMOLED")
    }
}

// ── PURPLE previews ───────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Purple – Light")
@Composable
private fun PreviewPurpleLight() {
    PreviewKitsuneTheme(variant = AppTheme.PURPLE, dark = false) {
        ThemeSwatch("PURPLE – Light")
    }
}

@Preview(showBackground = true, name = "Purple – Dark", backgroundColor = 0xFF171216)
@Composable
private fun PreviewPurpleDark() {
    PreviewKitsuneTheme(variant = AppTheme.PURPLE, dark = true) {
        ThemeSwatch("PURPLE – Dark")
    }
}

@Preview(showBackground = true, name = "Purple – AMOLED", backgroundColor = 0xFF000000)
@Composable
private fun PreviewPurpleAmoled() {
    PreviewKitsuneTheme(variant = AppTheme.PURPLE, dark = true, amoled = true) {
        ThemeSwatch("PURPLE – AMOLED")
    }
}

// ── BLUE previews ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Blue – Light")
@Composable
private fun PreviewBlueLight() {
    PreviewKitsuneTheme(variant = AppTheme.BLUE, dark = false) {
        ThemeSwatch("BLUE – Light")
    }
}

@Preview(showBackground = true, name = "Blue – Dark", backgroundColor = 0xFF111418)
@Composable
private fun PreviewBlueDark() {
    PreviewKitsuneTheme(variant = AppTheme.BLUE, dark = true) {
        ThemeSwatch("BLUE – Dark")
    }
}

@Preview(showBackground = true, name = "Blue – AMOLED", backgroundColor = 0xFF000000)
@Composable
private fun PreviewBlueAmoled() {
    PreviewKitsuneTheme(variant = AppTheme.BLUE, dark = true, amoled = true) {
        ThemeSwatch("BLUE – AMOLED")
    }
}

// ── GREEN previews ────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Green – Light")
@Composable
private fun PreviewGreenLight() {
    PreviewKitsuneTheme(variant = AppTheme.GREEN, dark = false) {
        ThemeSwatch("GREEN – Light")
    }
}

@Preview(showBackground = true, name = "Green – Dark", backgroundColor = 0xFF0F1512)
@Composable
private fun PreviewGreenDark() {
    PreviewKitsuneTheme(variant = AppTheme.GREEN, dark = true) {
        ThemeSwatch("GREEN – Dark")
    }
}

@Preview(showBackground = true, name = "Green – AMOLED", backgroundColor = 0xFF000000)
@Composable
private fun PreviewGreenAmoled() {
    PreviewKitsuneTheme(variant = AppTheme.GREEN, dark = true, amoled = true) {
        ThemeSwatch("GREEN – AMOLED")
    }
}

// ── All-in-one overview ───────────────────────────────────────────────────────

@Preview(showBackground = true, name = "All Variants – Light", widthDp = 420, heightDp = 900)
@Composable
private fun PreviewAllLight() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppTheme.entries.forEach { variant ->
            PreviewKitsuneTheme(variant = variant, dark = false) {
                Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.Gray))
                ThemeSwatch(variant.name)
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "All Variants – Dark",
    backgroundColor = 0xFF000000,
    widthDp = 420,
    heightDp = 900
)
@Composable
private fun PreviewAllDark() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppTheme.entries.forEach { variant ->
            PreviewKitsuneTheme(variant = variant, dark = true) {
                Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.DarkGray))
                ThemeSwatch("${variant.name} – Dark")
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "All Variants – AMOLED",
    backgroundColor = 0xFF000000,
    widthDp = 420,
    heightDp = 900
)
@Composable
private fun PreviewAllAmoled() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppTheme.entries.forEach { variant ->
            PreviewKitsuneTheme(variant = variant, dark = true, amoled = true) {
                Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.DarkGray))
                ThemeSwatch("${variant.name} – AMOLED")
            }
        }
    }
}
