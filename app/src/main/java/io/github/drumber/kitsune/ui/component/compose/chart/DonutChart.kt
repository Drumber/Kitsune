package io.github.drumber.kitsune.ui.component.compose.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/** Duration of the sweep-in animation, mirroring the former MPAndroidChart `animateY`. */
private const val ANIMATION_DURATION_MS = 800

/** Gap between slices, in degrees — the Compose equivalent of `PieDataSet.sliceSpace`. */
private const val SLICE_GAP_DEGREES = 1.5f

/** Fraction of the radius taken up by the hole, matching the former `holeRadius = 68f`. */
private const val HOLE_RADIUS_FRACTION = 0.68f

/** Extra radius applied to the selected slice, the equivalent of `selectionShift`. */
private const val SELECTION_SHIFT_FRACTION = 0.04f

private const val START_ANGLE_DEGREES = -90f
private const val FULL_CIRCLE_DEGREES = 360f

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * A Compose-native donut chart, replacing the MPAndroidChart `PieChart` used by the profile
 * statistics. Slices animate in on first composition and can be tapped to select one; the
 * selection is reported through [onSliceSelected] so the caller can show its own tooltip.
 *
 * @param slices          Slices to draw, in display order. Values need not sum to 100.
 * @param selectedIndex   Index of the highlighted slice, or `null` for none.
 * @param onSliceSelected Called with the tapped slice index, or `null` when tapping the hole
 *                        or a gap.
 * @param animate         Whether to run the sweep-in animation.
 * @param content         Drawn centred inside the hole (e.g. the chart title).
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onSliceSelected: (Int?) -> Unit = {},
    animate: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }
    val sweepProgress = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(slices, animate) {
        if (animate) {
            sweepProgress.snapTo(0f)
            sweepProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(ANIMATION_DURATION_MS, easing = EaseInOutQuad)
            )
        } else {
            sweepProgress.snapTo(1f)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(slices, total) {
                    detectTapGestures { tap ->
                        onSliceSelected(sliceIndexAt(tap, size.width, size.height, slices, total))
                    }
                }
        ) {
            if (total <= 0f) return@Canvas

            val diameter = min(size.width, size.height)
            val maxRadius = diameter / 2f
            val strokeWidth = maxRadius * (1f - HOLE_RADIUS_FRACTION)
            val baseRadius = maxRadius - strokeWidth / 2f

            var startAngle = START_ANGLE_DEGREES
            slices.forEachIndexed { index, slice ->
                val fullSweep = slice.value / total * FULL_CIRCLE_DEGREES
                val sweep = (fullSweep - SLICE_GAP_DEGREES).coerceAtLeast(0f) * sweepProgress.value
                val radius = if (index == selectedIndex) {
                    baseRadius + maxRadius * SELECTION_SHIFT_FRACTION
                } else {
                    baseRadius
                }

                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += fullSweep
            }
        }
        content()
    }
}

/**
 * Maps a tap position to the index of the slice underneath it, or `null` when the tap lands on
 * the hole, outside the chart, or in the gap between two slices.
 */
private fun sliceIndexAt(
    tap: Offset,
    width: Int,
    height: Int,
    slices: List<DonutSlice>,
    total: Float
): Int? {
    if (total <= 0f) return null

    val centerX = width / 2f
    val centerY = height / 2f
    val maxRadius = min(width, height) / 2f
    val distance = hypot(tap.x - centerX, tap.y - centerY)
    if (distance > maxRadius || distance < maxRadius * HOLE_RADIUS_FRACTION) return null

    val degrees = Math.toDegrees(
        atan2((tap.y - centerY).toDouble(), (tap.x - centerX).toDouble())
    ).toFloat()
    val normalized = (degrees - START_ANGLE_DEGREES + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES

    var startAngle = 0f
    slices.forEachIndexed { index, slice ->
        val sweep = slice.value / total * FULL_CIRCLE_DEGREES
        if (normalized >= startAngle && normalized < startAngle + sweep) return index
        startAngle += sweep
    }
    return null
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    KitsuneTheme {
        DonutChart(
            slices = listOf(
                DonutSlice("Action", 40f, Color(0xFFFEB700)),
                DonutSlice("Comedy", 25f, Color(0xFFFF9300)),
                DonutSlice("Drama", 20f, Color(0xFFFF3281)),
                DonutSlice("Romance", 15f, Color(0xFFBC6EDA))
            ),
            selectedIndex = 0,
            modifier = Modifier.size(220.dp)
        )
    }
}
