package io.github.drumber.kitsune.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.ColorRoles
import com.google.android.material.color.MaterialColors

private val current_seed = Color(0xFF00BCD4)
private val planned_seed = Color(0xFF2196F3)
private val completed_seed = Color(0xFF4CAF50)
private val onHold_seed = Color(0xFFFF9800)
private val dropped_seed = Color(0xFFF44336)

@Immutable
data class LibraryStatusColors(
    val current: Color = Color.Unspecified,
    val onCurrent: Color = Color.Unspecified,
    val currentContainer: Color = Color.Unspecified,
    val onCurrentContainer: Color = Color.Unspecified,
    val planned: Color = Color.Unspecified,
    val onPlanned: Color = Color.Unspecified,
    val plannedContainer: Color = Color.Unspecified,
    val onPlannedContainer: Color = Color.Unspecified,
    val completed: Color = Color.Unspecified,
    val onCompleted: Color = Color.Unspecified,
    val completedContainer: Color = Color.Unspecified,
    val onCompletedContainer: Color = Color.Unspecified,
    val onHold: Color = Color.Unspecified,
    val onOnHold: Color = Color.Unspecified,
    val onHoldContainer: Color = Color.Unspecified,
    val onOnHoldContainer: Color = Color.Unspecified,
    val dropped: Color = Color.Unspecified,
    val onDropped: Color = Color.Unspecified,
    val droppedContainer: Color = Color.Unspecified,
    val onDroppedContainer: Color = Color.Unspecified
)

val LocalLibraryStatusColors = staticCompositionLocalOf { LibraryStatusColors() }

@Composable
fun getLibraryStatusColors(isDarkTheme: Boolean, colorScheme: ColorScheme): LibraryStatusColors {
    val primary = colorScheme.primary

    val current = current_seed.getHarmonizedColorRoles(isDarkTheme, primary)
    val planned = planned_seed.getHarmonizedColorRoles(isDarkTheme, primary)
    val completed = completed_seed.getHarmonizedColorRoles(isDarkTheme, primary)
    val onHold = onHold_seed.getHarmonizedColorRoles(isDarkTheme, primary)
    val dropped = dropped_seed.getHarmonizedColorRoles(isDarkTheme, primary)

    return LibraryStatusColors(
        current = Color(current.accent),
        onCurrent = Color(current.onAccent),
        currentContainer = Color(current.accentContainer),
        onCurrentContainer = Color(current.onAccentContainer),
        planned = Color(planned.accent),
        onPlanned = Color(planned.onAccent),
        plannedContainer = Color(planned.accentContainer),
        onPlannedContainer = Color(planned.onAccentContainer),
        completed = Color(completed.accent),
        onCompleted = Color(completed.onAccent),
        completedContainer = Color(completed.accentContainer),
        onCompletedContainer = Color(completed.onAccentContainer),
        onHold = Color(onHold.accent),
        onOnHold = Color(onHold.onAccent),
        onHoldContainer = Color(onHold.accentContainer),
        onOnHoldContainer = Color(onHold.onAccentContainer),
        dropped = Color(dropped.accent),
        onDropped = Color(dropped.onAccent),
        droppedContainer = Color(dropped.accentContainer),
        onDroppedContainer = Color(dropped.onAccentContainer)
    )
}

private fun Color.getHarmonizedColorRoles(
    isDarkTheme: Boolean,
    colorToHarmonizeWith: Color
): ColorRoles {
    val harmonizedColor = MaterialColors.harmonize(this.toArgb(), colorToHarmonizeWith.toArgb())
    return MaterialColors.getColorRoles(harmonizedColor, !isDarkTheme)
}
