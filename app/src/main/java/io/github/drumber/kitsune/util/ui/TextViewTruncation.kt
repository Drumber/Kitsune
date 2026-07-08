package io.github.drumber.kitsune.util.ui

import android.text.StaticLayout
import android.widget.TextView

/**
 * Returns `true` if the [TextView]'s current text does not fully fit within its `maxLines`
 * constraint (i.e. it is visually truncated).
 *
 * We first check the layout's ellipsis count, but that can report `0` even when the text is cut
 * off — for example when truncation lands on a hard line break (`\n`). As a reliable fallback we
 * re-measure the full text with a [StaticLayout] at the view's content width and compare the
 * resulting line count against `maxLines`.
 *
 * Must be called after layout (e.g. from `doOnPreDraw`), when the view has a valid width and layout.
 */
fun TextView.isTextTruncated(): Boolean {
    val layout = layout ?: return false
    for (line in 0 until layout.lineCount) {
        if (layout.getEllipsisCount(line) > 0) return true
    }

    val maxLines = maxLines
    if (maxLines <= 0 || maxLines == Int.MAX_VALUE) return false

    val contentWidth = width - compoundPaddingLeft - compoundPaddingRight
    if (contentWidth <= 0 || text.isNullOrEmpty()) return false

    val fullLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, contentWidth)
        .setIncludePad(includeFontPadding)
        .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
        .setBreakStrategy(breakStrategy)
        .setHyphenationFrequency(hyphenationFrequency)
        .build()

    return fullLayout.lineCount > maxLines
}
