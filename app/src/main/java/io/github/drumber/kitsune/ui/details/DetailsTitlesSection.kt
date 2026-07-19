package io.github.drumber.kitsune.ui.details

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.common.en
import io.github.drumber.kitsune.data.common.withoutCommonTitles
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.databinding.ItemDetailsInfoRowBinding
import io.github.drumber.kitsune.util.DataUtil.mapLanguageCodesToDisplayName

/**
 * Encapsulates the dynamic alternative-titles rows in the details info table of [DetailsFragment],
 * including the 'show more'/'show less' toggle.
 *
 * The expanded state is owned by the caller (typically the view model) and accessed via
 * [isExpanded]/[setExpanded] so that it survives view recreation.
 */
class DetailsTitlesSection(
    private val binding: FragmentDetailsBinding,
    private val layoutInflater: LayoutInflater,
    private val resolveColor: (attrRes: Int) -> Int,
    private val isExpanded: () -> Boolean,
    private val setExpanded: (Boolean) -> Unit,
    private val currentTitles: () -> Titles?
) {

    companion object {
        private const val IDENTIFIER_TAG = "dynamic_title"
        private const val MAX_SHOWN_TITLES = 3
    }

    fun updateTitlesInDetailsTable(titles: Titles?) {
        val tableLayout = binding.sectionDetailsInfo.tableLayout
        // remove any previous added titles
        tableLayout.apply {
            children.filter { it.tag == IDENTIFIER_TAG }.toList().forEach { removeView(it) }
        }

        // map language codes and sort them
        val sortedTitles = titles?.withoutCommonTitles()
            ?.filterValues { !it.isNullOrBlank() }
            ?.filterNot { it.key == "en_us" && it.value == titles.en }
            ?.mapLanguageCodesToDisplayName()
            ?.toList()
            ?.sortedByDescending { it.first }

        if (sortedTitles.isNullOrEmpty()) return

        val shouldLimitShownTitles = sortedTitles.size > MAX_SHOWN_TITLES && !isExpanded()
        val rowIndex = tableLayout.indexOfChild(binding.sectionDetailsInfo.synonymsRowLayout.root)
            .coerceAtLeast(0)
        // add a row for each title
        sortedTitles
            .takeLast(if (shouldLimitShownTitles) MAX_SHOWN_TITLES else Int.MAX_VALUE)
            .forEach { (language, title) ->
                val rowBinding = ItemDetailsInfoRowBinding.inflate(layoutInflater)
                rowBinding.title = language
                rowBinding.value = title
                rowBinding.root.tag = IDENTIFIER_TAG
                tableLayout.addView(rowBinding.root, rowIndex)
            }

        // add 'show more' text to table
        if (sortedTitles.size > MAX_SHOWN_TITLES) {
            val showMoreRow = createShowMoreTitlesRow()
            showMoreRow.tag = IDENTIFIER_TAG
            val viewIndex =
                tableLayout.indexOfChild(binding.sectionDetailsInfo.synonymsRowLayout.root)
                    .coerceAtLeast(0)
            tableLayout.addView(showMoreRow, viewIndex)
        }
    }

    private fun createShowMoreTitlesRow(): View {
        val rowBinding = ItemDetailsInfoRowBinding.inflate(layoutInflater)
        val text = layoutInflater.context.getString(
            if (isExpanded()) {
                R.string.action_show_less
            } else {
                R.string.action_show_more
            }
        )
        rowBinding.title = SpannableString(text).apply {
            setSpan(
                ForegroundColorSpan(resolveColor(R.attr.colorPrimary)),
                0,
                text.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            setSpan(UnderlineSpan(), 0, text.length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        rowBinding.tvTitle.setOnClickListener {
            setExpanded(!isExpanded())
            updateTitlesInDetailsTable(currentTitles())
        }
        return rowBinding.root
    }
}
